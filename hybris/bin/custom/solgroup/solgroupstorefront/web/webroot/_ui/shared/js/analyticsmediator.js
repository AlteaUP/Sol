var mediator = (function() {
	var subscribe = function(tracker, fn) {
			try {
				if (!mediator.trackers[tracker])
				{
					mediator.trackers[tracker] = [];
				}
				mediator.trackers[tracker].push({context: this, callback: fn});
				return this;
			} catch(err) {
				return this;
			}
		},

		publish = function(tracker) {
			try {
				if (!mediator.trackers[tracker])
				{
					return false;
				}
				var args = Array.prototype.slice.call(arguments, 1);
				for (var i = 0, l = mediator.trackers[tracker].length; i < l; i++)
				{
					var subscription = mediator.trackers[tracker][i];
					subscription.callback.apply(subscription.context, args);
				}
				return this;
			} catch(err) {
				return this;
			}
		},

		publishAll = function() {
			try {
				if (Object.keys(mediator.trackers).length === 0)
				{
					return false;
				}
				for (var tracker in mediator.trackers)
				{
					var args = [tracker].concat(Array.prototype.slice.call(arguments));
					mediator.publish.apply(this, args);
				}
				return this;
			} catch(err) {
				return this;
			}
		};

	return {
		trackers: {},
		publish: publish,
		publishAll: publishAll,
		subscribe: subscribe
	}
})()