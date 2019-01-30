var accesses = new ObjectArrayHolder();
var TimeUnit = require("./timeunit");

Array.prototype.clean = function(deleteValue) {
  for (var i = 0; i < this.length; i++) {
    if (this[i] == deleteValue) {         
      this.splice(i, 1);
      i--;
    }
  }
  return this;
};

function Access() {
	this.timestamp = new Date().getTime();
	this.isOlder = function(time) {
		var now = new Date().getTime();
		return now > this.timestamp + time;
	}
}

function TimelyRestrict (time, maxAccess) {
	this.time = time;
	this.maxAccess = maxAccess;
	
	this.check = function(accesses) {
		if (!accesses || (accesses && accesses.length < this.maxAccess)) {
			return result(true, false, 0);
		}
		var firstAccess = accesses[0];
		var range = new Date().getTime() - firstAccess.timestamp;
		var access = range > this.time && accesses.length <= this.maxAccess;
		var removable = access;
		var minTimeForAccess = this.time - range;
		return result(access, removable, minTimeForAccess);
	};
}

function result(a, r, m) {
	return {access: a, removable: r, minTimeForAccess: m};
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

function ObjectArrayHolder () {
	this.objects = {};
	this.print = function() {console.log(this.objects);};
	this.getObjects = function () {return this.objects;};
	this.add = function (lover, object, access) {
		
		var current = this.objects[lover];
		
		if (!current) {
			current = {};
			this.objects[lover] = current;
		}
		
		var arr = current[object];
		
		if (!arr) {
			arr = [];
			current[object] = arr;
		}
		
		arr.push(access);
	};
	this.removeAll = function (k) {
		delete this.objects[k];
	};
	this.remove = function (key, o) {
		if (this.objects[key][o]) {
			delete this.objects[key][o];
		}
	};
	this.get = function(key, object) {
		if (this.objects[key])
			return this.objects[key][object];
	};
}

var AccessMonitorManagement = {
	check: function (lover, object, algorithm) {
		var objs = accesses.get(lover, object);		
		var result = algorithm.check(objs);

		if (result.removable) {
			accesses.remove(lover, object);
		}
		
		if (result.access) {
			accesses.add(lover, object, new Access());
			return true;
		} else {
			console.log(3, lover, object, result.minTimeForAccess);
			return result.minTimeForAccess;	
		}
	},
	getAccesses: function () {
		return accesses;	
	},
	getMinTimeForAccess: function (lover, obj, algorithm) {
		return algorithm.check(this.accesses.get(lover)).minTimeForAccess;
	},
	resetAll: function(lover) {
		accesses.removeAll(lover);
	},
	reset: function(lover, obj) {
		accesses.remove(lover, obj);
	}
}

var GarbageCollector = {
	check : function(obj, arr) {
		var max = GarbageCollector.getMax(obj);
		for (i in arr) {
			access = arr[i];
			if (access && access.timestamp > 0) {
				if (access.isOlder(max)) {
					delete arr[i];
					delete access;
				}
			}
		}	
		arr.clean(undefined);
	},
	do : function() {
		var objs = accesses.getObjects();
		for (var i in objs) {
			var o = objs[i];
			if (!o.hasOwnProperty()) {
				delete o;
				continue;
			}
			for (var j in o) {
				if (o[j].length == 0) {
					delete o[j];
					continue;
				} 
				GarbageCollector.check(j, o[j]);
			}
		}
	},
	getMax: function(obj) {
		if (obj === "sms" || obj === "call") {
			return TimeUnit.HOURS.toMillis(24);
		} else if (obj === "verification") {
			return TimeUnit.HOURS.toMillis(24);
		} else if (obj === "request") {
			return TimeUnit.HOURS.toMillis(24);
		}
	}
}

setInterval(GarbageCollector.do, TimeUnit.HOURS.toMillis(1));

module.exports = {AccessMonitorManagement, TimelyRestrict, ExponentialBackoff, ObjectArrayHolder, Access};

