/*
 * jQuery.liveFilter
 *
 * Copyright (c) 2009 Mike Merritt
 *
 * Forked by Lim Chee Aun (cheeaun.com)
 * 
 */
 
(function($){
        $.fn.liveFilter = function(inputEl, filterEl, options){
                var defaults = {
                        filterChildSelector: null,
                        filter: function(el, val){
                                return $(el).text().toUpperCase().indexOf(val.toUpperCase()) >= 0;
                        },
                        before: function(){},
                        after: function(){}
                };
                var options = $.extend(defaults, options);
                
                var el = $(this).find(filterEl);
                if (options.filterChildSelector) el = el.find(options.filterChildSelector);

                var filter = options.filter;
                $(inputEl).keyup(function(){
                        var val = $(this).val();
                        var contains = el.filter(function(){
                                return filter(this, val);
                        });
                        var containsNot = el.not(contains);
                        if (options.filterChildSelector){
                                contains = contains.parents(filterEl);
                                containsNot = containsNot.parents(filterEl).hide();
                        }
                        
                        options.before.call(this, contains, containsNot);
                        
                        contains.show();
                        containsNot.hide();
                        
                        if (val === '') {
                                contains.show();
                                containsNot.show();
                        }
                        
                        options.after.call(this, contains, containsNot);
                });
        }
})(jQuery);