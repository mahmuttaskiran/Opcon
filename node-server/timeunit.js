"use strict;"
var floor = Math.floor;
var TimeUnit = {
		MILLIS: {
            toMillis: function(value) {
                return value;
            },
            toSeconds: function(value) {
                return floor(value / 1000);
            },
            toMinutes: function(value) {
                return floor(value / (60 * 1000));
            },
            toHours: function(value) {
                return floor(value / (60 * 60 * 1000));
            },
            toDays: function(value) {
                return floor(value / (60 * 60 * 24 * 1000));
            }
        },
        SECONDS: {
            toMillis: function(value) {
                return value * 1000;
            },
            toSeconds: function(value) {
                return value;
            },
            toMinutes: function(value) {
                return floor(value / 60);
            },
            toHours: function(value) {
                return floor(value / (60 * 60));
            },
            toDays: function(value) {
                return floor(value / (60 * 60 * 24));
            }
        },
        MINUTES: {
            toMillis: function(value) {
                return value * 1000 * 60;
            },
            toSeconds: function(value) {
                return value * 60;
            },
            toMinutes: function(value) {
                return value;
            },
            toHours: function(value) {
                return floor(value / 60);
            },
            toDays: function(value) {
                return floor(value / (60 * 24));
            }
        },
        HOURS: {
            toMillis: function(value) {
                return value * 60 * 60 * 1000;
            },
            toSeconds: function(value) {
                return value * 60 * 60;
            },
            toMinutes: function(value) {
                return value * 60;
            },
            toHours: function(value) {
                return value;
            },
            toDays: function(value) {
                return floor(value / 24);
            }
        },
        DAYS: {
            toMillis: function(value) {
                return value * 60 * 60 * 1000 * 24;
            },
            toSeconds: function(value) {
                return value * 60 * 60 * 24;
            },
            toMinutes: function(value) {
                return value * 60 * 24;
            },
            toHours: function(value) {
                return value * 24;
            },
            toDays: function(value) {
                return value;
            }
        }
};
	
module.exports = TimeUnit;