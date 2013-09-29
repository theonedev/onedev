/*
 * jQuery File Upload Plugin JS Example 8.8.2
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, regexp: true */
/*global $, window, blueimp */

$(function () {
    'use strict';

    // Initialize the jQuery File Upload widget:
    $('#${componentId}').fileupload({
        // Uncomment the following to send cross-domain cookies:
        //xhrFields: {withCredentials: true},
        url: '${url}'
    });

    // Load existing files:
    $('#${componentId}').addClass('fileupload-processing');
    $.ajax({
        // Uncomment the following to send cross-domain cookies:
        //xhrFields: {withCredentials: true},
        url: $('#${componentId}').fileupload('option', 'url'),
        dataType: 'json',
        context: $('#${componentId}')[0]
    }).always(function () {
        $(this).removeClass('fileupload-processing');
    }).done(function (result) {
    	console.log('done');
        $(this).fileupload('option', 'done')
            .call(this, null, {result: result});
    });

});
