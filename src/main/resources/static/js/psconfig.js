$(function() {
 var moveLeft = 20;
  var moveDown = -100;

  $('a.trigger').hover(function(e) {
	  var id = $(this).attr('id');
	  //alert(id);
  
    $('div#'+id).show();
      //.css('top', e.pageY + moveDown)
      //.css('left', e.pageX + moveLeft)
      //.appendTo('body');
  }, function() {
	  var id = $(this).attr('id');  
    $('div#'+id).hide();
  });

  $('a.trigger').mousemove(function(e) {
	  var id = $(this).attr('id');
    $("div#" +id).css('top', e.pageY + moveDown).css('left', e.pageX + moveLeft);
  });

});



