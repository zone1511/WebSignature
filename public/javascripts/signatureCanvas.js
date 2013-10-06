function signatureCanvas(element) {

  this.canvas = element[0];
  this.context = canvas.getContext('2d');

  this.penPosition = {x: 0, y: 0};
  this.lastPenPosition = {x: 0, y: 0};
  this.trace = [];

  this.signatureStarted = false;
  this.painting = false;

  this.context.lineWidth = 5;
  this.context.lineJoin = 'round';
  this.context.lineCap = 'round';
  this.context.strokeStyle = 'blue';


  this.startSampling = function(freq) {
    console.log("Sampling frequency : "+freq+"Hz");
    console.log("Equivalent interval : "+(1.0/freq)*1000+"ms");
    setInterval(function(){
        if(painting) {
          trace.push({x : penPosition.x, y : penPosition.y});
        } else {
          trace.push({x : 0, y : 0});
        }
      },(1.0/freq)*1000);
  };

  this.canvasListeners = function(signals, listener, add) {
    var events = signals.split(' ');
    for (var i=0, nbEvents=events.length; i<nbEvents; i++) {
      if(add) {
        canvas.addEventListener(events[i], listener, false);
      } else {
        canvas.removeEventListener(events[i], listener, false);
      }
    }
  };

  this.updatePenPosition = function(event) {
    event.preventDefault();

    lastPenPosition.x = penPosition.x;
    lastPenPosition.y = penPosition.y;

    if(event.type.indexOf("touch") >= 0) {
      penPosition.x = event.targetTouches[0].pageX - canvas.offsetLeft;
      penPosition.y = event.targetTouches[0].pageY - canvas.offsetTop;
    } else {
      penPosition.x = event.pageX - canvas.offsetLeft;
      penPosition.y = event.pageY - canvas.offsetTop;
    }
  };

  this.onPaint = function(event) {
    event.preventDefault();

    context.beginPath();
    context.moveTo(lastPenPosition.x, lastPenPosition.y);
    context.lineTo(penPosition.x, penPosition.y);
    context.closePath();
    context.stroke();
  };

  this.onInteraction = function(event) {
    event.preventDefault();

    if(!signatureStarted) {
      startSampling(100);
      signatureStarted = true;
    }

    updatePenPosition(event);

    painting = true;
    canvasListeners(
      'mousemove touchmove',
      onPaint, true
    );
  };

  this.onStopInteraction = function(event) {
    event.preventDefault();
    painting = false;
    canvasListeners(
      'mousemove touchmove',
      onPaint, false
    );
  };

  canvasListeners(
    'mousemove touchmove',
    updatePenPosition, true
  );

  canvasListeners(
    'mousedown touchstart',
    onInteraction, true
  );

  canvasListeners(
    'mouseup touchend touchcancel touchleave',
    onStopInteraction, true
  );
}