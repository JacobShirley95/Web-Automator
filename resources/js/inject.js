window.injectionLoaded = true;

println("TEST TEST TEST");

$ = jQuery;

function Offset() {
	this.left = 0;
	this.top = 0;
}

$.fn.offset2 = function() {
    var offset2 = this.offset();

	offset2.left += this.iframeOff.left;
	offset2.top += this.iframeOff.top;
	
    return offset2;
};

$.fn.iframeOff = new Offset();

var oldFind = $.fn.find;

$.fn.find = function(sel) {
	return findElementsInIFrames(this, sel);
}

$.fn.findVisibles = function(sel) {
	return findVisibleElements(this, sel);
}

function sendBackResults(results) {
	var offX = window.pageXOffset;
	var offY = window.pageYOffset;
	results.each(function (i) {
		var el = $(this);
		var off = el.offset2();
		
		var x = off.left;
		var y = off.top;
				
		var width = getElementWidth(el);
		var height = getElementHeight(el);
		
		tabCallback(i, x, y, width, height, this);
	});
	return null;
}

function getJSClickables() {
	return $("div").filter(function(i) {
		if (this.onclick != null) 
			return true;
		}).findVisibles("*");
}

function removeAllButOne(elems) {
	elems.each(function (i) {
		if (i > 0)
			$(this).remove();
	});
	return null;
}

function removeAll(elems) {
	elems.empty();
	return null;
}

function move(elems, parent) {
	parent.append(elems.detach());
}

function _findElementsInIFrames(parent, sel, offset) {
	var el = oldFind.call(parent, sel);
    
	if (el.length > 0) {
		el.each(function (i) {
			var $this = $(this);
			
			$this.iframeOff.left = offset.left;
			$this.iframeOff.top = offset.top;
		});
	} else {
    	oldFind.call(parent, "iframe").each(function(i) {
    		var contents = null;
    		var off = null;
    		try {	
    			var $this = $(this);
    			contents = $this.contents();
    			
		    	off = $this.offset();
		    	off.left += offset.left;
		    	off.top += offset.top;
    		} catch (e) {
    			contents = null;
    		}
    		
    		if (contents != null) {
	    		var temp = _findElementsInIFrames(contents, sel, off);
		    	
	    		if (temp.length > 0) {
	    			el = el.add(temp);
		    	}
	    	}
    	});
	}
	
	return el;
}

function findElementsInIFrames(parent, sel) {
	var offset = new Offset();
	
	var els = _findElementsInIFrames(parent, sel, offset);
	
	return els;
}

function getElements(parent, jquerySelector) {
	return findElementsInIFrames(parent, jquerySelector);//getVisibleElements(jquerySelector);
}

function findVisibleElements(parent, jquerySelector) {
	var pageOffX = window.pageXOffset;
	var pageOffY = window.pageYOffset;
	
	var pageWidth = $(window).width();
	var pageHeight = $(window).height();

	return findElementsInIFrames(parent, jquerySelector).filter(function(index) {
		var $this = $(this);
		var offset = $this.offset();
		
		var offset2 = $this.offset2();
		
		var width = getElementWidth($this);
		var height = getElementHeight($this);
		
		var x = offset2.left - pageOffX;
		var y = offset2.top - pageOffY;
		
		if (x >= 0 && y >= 0) {
			var el = elementFromPointIFrames(x, y, offset.left, offset.top);
			//println("x "+x+", y "+y+", "+document.elementFromPoint(x, y));
			
			if (el == this) {
				return true;
			} else {
				var elems = elementsFromPointIFrames(x, y, offset.left, offset.top);
				
				for (var i = 0; i < elems.length; i++) {
					if (elems[i] == this) {
						return true;
					}
				}
			}
		}
	});
}

function elementFromPointIFrames(x, y, relativeX, relativeY) {
	var el = document.elementFromPoint(x, y);
	
	if (el instanceof HTMLIFrameElement) {
		var win = el.contentWindow;
		var doc = win.document;
    	el = doc.elementFromPoint(relativeX-win.pageXOffset, relativeY-win.pageYOffset);
    }
    return el;
}

function elementsFromPointIFrames(x, y, relativeX, relativeY) {
	var results = [];
	var elems = document.elementsFromPoint(x, y);
	for (var i = 0; i < elems.length; i++) {
		var el = elems[i];
		if (el instanceof HTMLIFrameElement) {
			var win = el.contentWindow;
			var doc = win.document;
			$.merge(results, doc.elementsFromPoint(relativeX-win.pageXOffset, relativeY-win.pageYOffset));
		} else results.push(el);
    }
    return results;
}

function getElementWidth(elem) {
	var width = elem.outerWidth();
	var offset = elem.offset2();
	elem.children().each(function (i) {
		var $this = $(this);
		if ($this.offset2().left >= offset.left)
			width = Math.max(width, getElementWidth($(this)));
	});
	return width;
}

function getElementHeight(elem) {
	var height = elem.outerHeight();
	var offset = elem.offset2();
	elem.children().each(function (i) {
		var $this = $(this);
		if ($this.offset2().top >= offset.top)
			height = Math.max(height, getElementHeight($(this)));
	});
	return height;
}

//AD CLICKER STUFF

function getAdURL() {
	var finalHref = null;
	$("a").each(function() {
		var href = this.href;
		if (href.indexOf("&adurl=") > 0) {
			finalHref = href;
			return false;
		}
	});
	return finalHref;
}

function moveOtherElements() {
	var c = 8888888;
	$('.adsbygoogle').each(function() {
		var $this = $(this);
		$this.parentsUntil('body').css('position', 'relative');
		//$this.css('position', 'fixed').css('display', 'block').css('z-index', ""+c).css('left', '0px').css('top', '0px').show();
		//$this.detach().appendTo("body");
		c += 1111111;
	});
}

//println("Code injected successfully");