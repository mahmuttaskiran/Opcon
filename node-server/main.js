// this file contains secret keys and api tokens.
// create one!
var Constants = require('./Constants.js');

var am = require("./access");
var TimeUnit = require("./timeunit");
var express = require("express");
var eapp = express();
var authy = require("./authy")(Constants.TWILVO_AUTHY_TOKEN);
var firebase = require("firebase-admin");
var databaseurl = "https://opcon-ccf1d.firebaseio.com/";
var randomstring = require("randomstring");
var Accountkit = require ('node-accountkit');
var PNF = require('google-libphonenumber').PhoneNumberFormat;
// Get an instance of `PhoneNumberUtil`.
var phoneUtil = require('google-libphonenumber').PhoneNumberUtil.getInstance();

var winston = require('winston');





// init logging utils.
// thanks winston!
var INFO = new winston.Logger({
    level: 'info',
    transports: [
      new (winston.transports.File)({ filename: 'info.log'}),
	  new (winston.transports.Console)()
    ]
});

var VERBOSE = new winston.Logger({
    level: 'verbose',
    transports: [
      new (winston.transports.File)({ filename: 'verbose.log'}),
	  new (winston.transports.Console)()
    ]
});

var ERROR = new winston.Logger({
    level: 'error',
    transports: [
      new (winston.transports.File)({ filename: 'error.log'}),
	  new (winston.transports.Console)()
    ]
});

function __info(info){INFO.log('info', info)}
function __error(err) {ERROR.log('error', err)}
function __verbose(verbose) {VERBOSE.log('verbose', verbose)}


Accountkit.set (Constants.ACCOUNT_KIT_APPID, Constants.ACCOUNT_KIT_TOKEN); //API_VERSION is optional, default = v1.1
Accountkit.requireAppSecret (true); // if you have enabled this option, default = true 

firebase.initializeApp({
  credential: firebase.credential.cert("files/admin.json"),
  databaseURL: databaseurl
});

firebase.database.enableLogging(false);
process.on("uncaughtException", (err) => {console.log("uncaughtException", err);});

var R_IP = new am.TimelyRestrict(TimeUnit.HOURS.toMillis(10), 10);
var R_VERIFY = new am.ExponentialBackoff(TimeUnit.SECONDS.toMillis(60), TimeUnit.HOURS.toMillis(2), 2);
var R_CALL = new am.ExponentialBackoff(TimeUnit.SECONDS.toMillis(120), TimeUnit.HOURS.toMillis(24));
var R_SMS = new am.ExponentialBackoff(TimeUnit.MINUTES.toMillis(2), TimeUnit.HOURS.toMillis(24));

const SMS = "sms";
const CALL = "call";
const VERIFICATION = "verification";
const REQUEST = "request";

const BAD_REQUEST = "bad_request";
const INVALID_CODE = "invalid_code";

const RELEASE_MODE = true;
const __SECRET__ = "testingOpconServer-XYZ";

// server test in root node.
eapp.get("/", (r, s) => {
	s.send("Hi, " + randomstring.generate() + ".");
	s.end();
});


// show all accesses (that stored for restrict management)
eapp.get("/accesses", (req, res) => {
	
	if (RELEASE_MODE) {
		return;
	}
	
	__info("accessed accesses")
	
	res.send(am.AccessMonitorManagement.getAccesses().getObjects());
	res.end();
});



// set firebase logging
eapp.get("/testmode", (req,res) => {
	
	if(RELEASE_MODE) {
		return;
	}
	
	__info("accessed testmode")
	
	var b = req.query.bool;
	if (b!==null) {
		firebase.database.enableLogging(b);
	}
});

// firebase database test
eapp.get("/database", (req, res) => {
	if (req.query.secret === __SECRET__) {
		
		__info("accessed database for test.")
		
		var random = randomstring.generate();
		firebase.database().ref("test/test").once("value").then((s) => {
			console.log("then");
			var equal = random === s.val();
			res.send(equal +  ", " + random + "===" + s.val());
			res.end();
		});
		firebase.database().ref("test/test").set(random);
		console.log("setted!");
	}
});

// authy test
eapp.get("/authy", (r, s) => {
	
	if (r.query.secret !== __SECRET__) {
		return;
	}
	
	__info("accessed authy test")
	
	var phone = r.query.phone;
	var locale = r.query.locale;
	var method = r.query.method;
	var dial_code = r.query.code;
	
	if (isUndefinedOrNull(phone))
		phone = "+905462272550";
	if (isUndefinedOrNull(locale))
		locale = "TR";
	if (isUndefinedOrNull(method))
		method = "call";
	if (isUndefinedOrNull(dial_code))
		dial_code = "90";
	
	authy.phones().verification_start(locale, phone, dial_code, method, (code, err, a_response) => 
	{
		var result = {
			code:code,
			err:err,
			response: a_response
		};
		s.send(result);
		s.end();
	});
	
});

// for testing
eapp.get("/test", (r, s) => test(r,s));

// to important service: request, verify
eapp.get("/request", (r, s) => tokenRequest(r,s));
eapp.get("/verify", (r, s) => tokenVerify(r,s));

// listen
eapp.listen(8080, () => console.log("listening..."));

// aliases
var database = firebase.database();
var fcm = database.ref("fcm");
var messasing = firebase.messaging();

// listen fcm node
fcm.on("child_added", (data) => fcmReceived(data));

function fcmReceived(data) {
	// uer uid
	var target = data.val().target;
	// path of node. example: msgs/+905462272550
	var path = data.val().path;
	// uid of node.
	var key = data.val().key;
	
	if (!target || !path || !key) {
		// invalid request. remove fcm node.
		data.ref.remove();
		return;
	}
	
	// get user fcm token.
	database.ref("users/" + target + "/fcm_token").once("value").then((s) => {
		if (s.val() !== null) {
			// get data.
			database.ref(path + "/" + key).once("value").then((node) => {
				if (node === null || node === undefined || node.val() === null) {
					// target node is removed. so, remove fcm node.
					data.ref.remove();
				} else {
					var msg = {
						data: {
							data: JSON.stringify(node.val()),
							path: path,
							sid: key
						}
					};
					
					firebase.messaging().sendToDevice(s.val(), msg);
					// message delivered. remove fcm node.
					// data.ref.remove();
				}
			});
			
		} else {
			console.log("fcm token returned null. data removed!");
			// invalid user. remove fcm node.
			data.ref.remove();
		}
	});
}

function tokenVerify(request, response) {
	var ip_address = request.connection.remoteAddress;
	var phone, locale, dial_code, token;
	var method;

	method = request.query.method;	
	phone = request.query.phone;
	locale = request.query.locale;
	dial_code = request.query.dial_code;
	token = request.query.token;

	
	// if method is "account_kit" there is no
	// mather other params
	
	VERBOSE.log('verbose', "tokenVerify-> method(%s)," + 
				"phone(%s), locale(%s), dial_code(%s)" + 
			   "token(%s)", method, phone, locale,
			   dial_code, token);
	
	
	if (method === "account_kit" && !token) {
		response.send(BAD_REQUEST);
		response.end();
	} else {
		if (isUndefinedOrNull(phone) || isUndefinedOrNull(locale) || 	isUndefinedOrNull(dial_code)) {
			response.send(BAD_REQUEST);
			response.end();
			return;
		}
	} 

	if (method !== "account_kit") {
		var av = am.AccessMonitorManagement.check(phone, VERIFICATION, R_VERIFY);
		if (av !== true) {
			response.send({"verification": av});
			response.end();
			VERBOSE.log("verbose", "request is backoffed (%s)", phone);
			return;
		}
	}
	
	if (method === "account_kit") {
		Accountkit.getAccountInfo (token, function(err, resp) {
			/**
			{
				"email": {
					"address": "mail.goyalshubham@gmail.com"
				},
				"id": "941488975973375"
			}
			*/
			if (!err && resp != null && resp != undefined && resp.phone.number) {
				VERBOSE.log("verbose", "success account_kit (%s)", resp.phone.number);
				tokenSuccess(formatNumber(resp.phone.number,locale), response);
			} else {
				ERROR.log("error", "account_kit error (pn: %s, token: %s)", phone, token);
				ERROR.log("error is %j or %s", err, err);
				response.send("account_kit_error");
				response.end();
			}
		});
	} else {
		authy.phones().verification_check(phone, dial_code, token, (code, err, a_response) => {
			if (code === 200 && isDefinedAndNonNull(a_response) && a_response.success) {
				tokenSuccess(phone, response);
				VERBOSE.log("twilio_success (%s)", phone);
			} else if (code === 401) {
				response.send(INVALID_CODE);
				response.end();
				VERBOSE.log("verbose","twilio_invalid_code (%s)", phone);
			} else {
				response.send("failed");
				response.end();
				console.log();
				ERROR.log("error", "twilio_failed(%s) %j or %s",phone, err, err);
			}
		});
	}
}

function formatNumber(number, locale) {
	var phoneNumber = phoneUtil.parse(number, locale);	
	var result =  phoneUtil.format(phoneNumber, PNF.INTERNATIONAL);
	if (result == null) return number; else return result;
}

function standartFormat(number) {
	var result = number.replace(new RegExp("[^0-9]", "g"), "")
	return "+" + result;
}

function tokenSuccess(phone, response) {
	// delete all spaces in phone
	phone = standartFormat(phone);
	var uid = phone;
	var email = getValidEmailAddress();
	firebase.auth().getUser(uid)
  	.then((userRecord) => {
		// user is already exixts. update it.
	    updateUser(userRecord, uid, email, response);
  	})
  	.catch((error) => {
		// create new user.
		createUser(phone, email, response);
  	});
}

function createUser(phone, email, response) {
	var uid = phone;
	var pass = getRandomPassword();
	firebase.auth().createUser({
	  uid: uid,	
	  email: email,
	  emailVerified: true,
	  password: pass
	}).then((userRecord) => {
		VERBOSE.log("verbose", "user created (email: %s, phone: %s, uid: %s)",
					email, phone, uid);
		response.send({
			token:email,
			password:pass
		});
		response.end();
	}).catch((error) => {
		ERROR.log("error", "user cannot created (email: %s, phone: %s, uid: %s)",
				  email, phone, uid);
		response.send({
			error:error
		});
		response.end();
	});
}

function updateUser(userRecord, phone, email, response) {
	var uid = phone;
	var newPassword = getRandomPassword();
		firebase.auth().updateUser(uid, {
		  email: email,
		  emailVerified: true,
		  password: newPassword
		})
	  .then(function(userRecord) {
		VERBOSE.log("verbose", "user updated (email: %s, phone: %s, uid: %s)",
					email, phone, uid);
		response.send({
			token:email,
			password: newPassword
		});
		response.end();
	  })
	  .catch(function(error) {
		ERROR.log("error", "user cannot updated (email: %s, phone: %s, uid: %s)",
				  email, phone, uid);
		response.send({
			error:error
		});
		response.end();
	  });
}


function tokenRequest(request, response) {
	var ip = request.connection.remoteAddress;
	var phone, locale, dial_code, method;
	
	phone = request.query.phone;
	locale = request.query.locale;
	dial_code = request.query.dial_code;
	method = request.query.method;
	
	VERBOSE.log('verbose', "tokenRequest-> method(%s)," + 
				"phone(%s), locale(%s), dial_code(%s)",
				method, phone, locale, dial_code);
	
	if (isUndefinedOrNull(phone) || isUndefinedOrNull(locale) || isUndefinedOrNull(dial_code) || isUndefinedOrNull(method)) {
		response.send(BAD_REQUEST);
		response.end();
		return;
	}
	
	var ipAccess = am.AccessMonitorManagement.check(ip, REQUEST, R_IP);
	
	if (ipAccess !== true) {
		response.send({"request": ipAccess});
		response.end();
		__verbose("request backoffed ip:" + ip);
		return;
	}
	
	var phoneAccess = am.AccessMonitorManagement.check(phone, method, method === "sms" ? R_SMS : R_CALL);
	
	if (phoneAccess !== true) {
		response.send({method: method, backoff: phoneAccess});
		response.end();
		__verbose("request backoffed phone: " + phone + " method: " + method);
		return;
	}
	
	authy.phones().verification_start(locale, phone, dial_code, method,
									 (code, err, a_response) => {
		if (isDefinedAndNonNull(a_response) && a_response.success) {
			am.AccessMonitorManagement.reset(phone, VERIFICATION);
			response.send("success");
			response.end();
			__verbose("request_started_for_" + phone);
			return;
		}
		response.send("failed");
		response.end();
		ERROR.log("error", "twilio_request_error %j or %s", err);
	});
}

function isDefinedAndNonNull(anyObject){
	return (anyObject !== null && anyObject !== undefined)
}

function isUndefinedOrNull(anyObject) {
	return anyObject === undefined || anyObject == null;
}

function test(request, response) {
	if (request.query.secret !== __SECRET__ && !RELEASE_MODE) {
		return;
	}
	var phone = request.query.phone;
	var locale = request.query.locale;
	var dial_code = request.query.dial_code;
	var addition = {phone: phone,locale: locale,dial_code: dial_code};	
	var email = getValidEmailAddress();
	tokenSuccess(phone, response);
}

function getValidEmailAddress() {
	var r = randomstring.generate({length: 40,charset: 'alphanumeric'});
	return r + "@opcon.com";
}

function getRandomPassword() {
	return randomstring.generate({length: 40,charset: 'alphanumeric'});
}
