function SignaturePad(element) {

  this.canvas = element[0];
  this.context = canvas.getContext('2d');

  this.penPosition = {x: 0, y: 0, t: 0};
  this.lastPenPosition = {x: 0, y: 0, t: 0};
  this.trace = [];

  this.signatureStarted = false;
  this.painting = false;

  this.context.lineWidth = 5;
  this.context.lineJoin = 'round';
  this.context.lineCap = 'round';
  this.context.strokeStyle = '#072847';

  this.sampler = null;
  this.timeout = null;
  this.start = null;

  this.startSampling = function(freq) {
    console.log("Sampling frequency : "+freq+"Hz");
    console.log("Equivalent interval : "+(1.0/freq)*1000+"ms");
    this.trace = [];
    this.boundSampler = this.sample.bind(this);
    this.sampler = setInterval(this.boundSampler, (1.0/freq)*1000);
  };

  this.sample = function() {
    var t = new Date().getTime() - this.start;
    if(this.painting) {
      this.trace.push({x : this.penPosition.x, y : this.penPosition.y, t : t});
    } else {
      //trace.push({x : -1, y : -1, t : t});
    }
  };

  this.sendSign = function(username, add_success, error) {
    $.ajax({
      url: jsRoutes.controllers.Enrollment.addEnrollmentSignature().url,
      type: "POST",
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      data: JSON.stringify({
        name: username,
        signature: this.trace
      })
    }).done(function() {
      add_success();
    }).fail(function() {
      error();
    });
    this.clear();
  };

  this.findProba = function(username, check_success, error) {
    $.ajax({
      url: jsRoutes.controllers.Enrollment.probaSignature().url,
      type: "POST",
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      data: JSON.stringify({
        name: username,
        signature: this.trace
      })
    }).done(function(data) {
      check_success(data.probability);
    }).fail(function() {
      error();
    });
    this.clear();
  };

  this.enroll = function(enrollment_pending, enrollment_success, error) {
    enrollment_pending();
    $.ajax({
      url: jsRoutes.controllers.Enrollment.enroll().url,
      type: "POST"
    }).done(function() {
      enrollment_success();
    }).fail(function() {
      error();
    });
  }

  this.canvasListeners = function(signals, listener, add) {
    var events = signals.split(' ');
    for (var i=0, nbEvents=events.length; i<nbEvents; i++) {
      if(add) {
        this.canvas.addEventListener(events[i], listener, false);
      } else {
        this.canvas.removeEventListener(events[i], listener, false);
      }
    }
  };

  this.updatePenPosition = function(event) {
    event.preventDefault();

    this.lastPenPosition.x = this.penPosition.x;
    this.lastPenPosition.y = this.penPosition.y;

    if(event.type.indexOf("touch") >= 0) {
      this.penPosition.x = event.targetTouches[0].pageX - this.canvas.offsetLeft;
      this.penPosition.y = event.targetTouches[0].pageY - this.canvas.offsetTop;
    } else {
      this.penPosition.x = event.pageX - this.canvas.offsetLeft;
      this.penPosition.y = event.pageY - this.canvas.offsetTop;
    }
  };

  this.onPaint = function(event) {
    event.preventDefault();

    this.context.beginPath();
    this.context.moveTo(this.lastPenPosition.x, this.lastPenPosition.y);
    this.context.lineTo(this.penPosition.x, this.penPosition.y);
    this.context.closePath();
    this.context.stroke();
  };

  this.onInteraction = function(event) {
    event.preventDefault();

    this.trace.push({x : -1, y : -1, t : -1});

    if(!this.signatureStarted) {
      this.start = new Date().getTime();
      this.startSampling(100);
      this.signatureStarted = true;
    }

    this.updatePenPosition(event);

    this.painting = true;
    //It is not possible to remove binded listners if we do not keep track of them
    //See http://stackoverflow.com/questions/11565471/removing-event-listener-which-was-added-with-bind
    this.painter = this.onPaint.bind(this);
    this.canvasListeners(
      'mousemove touchmove',
      this.painter, true
    );
  };

  this.onStopInteraction = function(event) {
    event.preventDefault();
    this.painting = false;
    this.canvasListeners(
      'mousemove touchmove',
      this.painter, false
    );
  };

  this.clear = function() {
    clearInterval(this.boundSampler);
    //clearTimeout(timeout);
    this.signatureStarted = false;
    this.context.setTransform(1, 0, 0, 1, 0, 0);
    this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.trace = [];
  };

  this.canvasListeners(
    'mousemove touchmove',
    this.updatePenPosition.bind(this), true
  );

  this.canvasListeners(
    'mousedown touchstart',
    this.onInteraction.bind(this), true
  );

  this.canvasListeners(
    'mouseup touchend touchcancel touchleave',
    this.onStopInteraction.bind(this), true
  );
}