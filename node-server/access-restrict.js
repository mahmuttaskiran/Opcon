"use strict";
var sqlite3 = require("sqlite3").verbose();
var DATABASE_NAME = "db/access_monitor.db";

function closeCallback(err) {
	console.log(err);
}

function closeDatabase(database) {
	database.close(closeCallback);
}

var AccessStore = {
	
	init: function () {
		var database = this.getNewDatabase();
		database.get("select * from accesses where timestamp <> ?", [-1], function(err, row) {
				if (err) {
					console.log("table created.");
					database.run("create table accesses (lover varchar(100), timestamp integer, object varchar(30))", (e) => {console.log(e); });
				} else {
					console.log("table is already exists.");
				}
				closeDatabase(database);
			});
	},
	
	addAccess: function(lover, obj, time) {
		var database = this.getNewDatabase();
		database.run("insert into accesses values (?,?,?)", [lover, time, obj], (e) => {console.log(e); });	
		closeDatabase(database);
	},
	
	removeAccesses:function(lover, object) {
		var database = this.getNewDatabase();
		database.run("delete from accesses where lover = ? and object = ?", [lover, object], (err) => {console.log(err); });
		closeDatabase(database);
	},
	
	getAccess:function(forLover, forObject, callback) {
		var database = this.getNewDatabase();
		database.all("select * from accesses where lover = ? and object = ?", [forLover, forObject], (err, rows) => {
			callback(err, rows);
			closeDatabase(database);
		});
	},
	
	getNewDatabase: function() {
		return new sqlite3.Database(DATABASE_NAME);
	}
};

function canAccess(lover, object, checker, callback) {
	var now = new Date().getTime();
	AccessStore.getAccess(lover, object, (err, rows) => {
		if (!err && rows!=undefined && rows.length > 0) {
			var result = checker.check(rows);
			if (result.removable) {
				AccessStore.removeAccesses(lover, object);
			}
			callback(result.access, result.minTimeForAccess);
		} else {
			callback(true, 0);
		}
		if (checker instanceof ExponentialBackoff && result && !result.access) {

		} else {
			AccessStore.addAccess(lover, object, now);
		}
	});
}

function removeRestrict(lover, object) {

	AccessStore.removeAccesses(lover, object);

}

function get_min_time(lover, object, checker, callback) {
	AccessStore.getAccess(lover, object, function(err, rows) {
		if (!err) {
			callback(checker.check(rows).minTimeForAccess);
		} else {
			callback(0);
		}
	});
}

var result = function(a, r, m) {
	return {access: a, removable: r, minTimeForAccess: m};
};
	

function TimelyRestrict (time, maxAccess) {
	this.time = time;
	this.maxAccess = maxAccess;
	
	this.check = function(accesses) {
		if (accesses.length < this.maxAccess) {
			return result(true, false, 0);
		}
		var firstAccess = accesses[0];
		var range = new Date().getTime() - firstAccess.timestamp;
		var access = range > this.time && access.length <= this.maxAccess;
		var removable = access;
		var minTimeForAccess = this.time - range;
		return result(access, removable, minTimeForAccess);
	};
}

function ExponentialBackoff (additionTime, maxTime, delayCount) {
	if (!delayCount) {
		delayCount = 0;
	}
	
	this.delayCount = delayCount;
	this.additionTime = additionTime;
	this.maxTime = maxTime;

	this.check = function(accesses) {
		if (!accesses || accesses.length < this.delayCount || accesses.length < 1) {
			return result(true, false, 0);
		}
		var totalExtime = (accesses.length) * this.additionTime;
		var now = new Date().getTime();
		var lastAccessTime = accesses[accesses.length -1].timestamp;
		var access, removable, minTimeForAccess;
		access = now > lastAccessTime + totalExtime;
		removable = totalExtime > this.maxTime;
		minTimeForAccess = totalExtime - (now - lastAccessTime);
		return result(access, removable, minTimeForAccess);
	};
}

AccessStore.init();

module.exports = {ExponentialBackoff, TimelyRestrict, canAccess, get_min_time, removeRestrict};