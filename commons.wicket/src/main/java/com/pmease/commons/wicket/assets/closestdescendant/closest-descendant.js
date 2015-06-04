/*!
 * .closestDescendant( selector [, findAll ] )
 * https://github.com/tlindig/jquery-closest-descendant
 *
 * v0.1.2 - 2014-02-17
 *
 * Copyright (c) 2014 Tobias Lindig
 * http://tlindig.de/
 *
 * License: MIT
 *
 * Author: Tobias Lindig <dev@tlindig.de>
 */
(function($) {

    /**
     * Get the first element(s) that matches the selector by traversing down
     * through descendants in the DOM tree level by level. It use a breadth
     * first search (BFS), that mean it will stop search and not going deeper in
     * the current subtree if the first matching descendant was found.
     *
     * @param  {selectors} selector -required- a jQuery selector
     * @param  {boolean} findAll -optional- default is false, if true, every
     *                           subtree will be visited until first match
     * @return {jQuery} matched element(s)
     */
    $.fn.closestDescendant = function(selector, findAll) {

        if (!selector || selector === '') {
            return $();
        }

        findAll = findAll ? true : false;

        var resultSet = $();

        this.each(function() {

            var $this = $(this);

            // breadth first search for every matched node,
            // go deeper, until a child was found in the current subtree or the leave was reached.
            var queue = [];
            queue.push($this);
            while (queue.length > 0) {
                var node = queue.shift();
                var children = node.children();
                for (var i = 0; i < children.length; ++i) {
                    var $child = $(children[i]);
                    if ($child.is(selector)) {
                        resultSet.push($child[0]); //well, we found one
                        if (!findAll) {
                            return false; //stop processing
                        }
                    } else {
                        queue.push($child); //go deeper
                    }
                }
            }
        });

        return resultSet;
    };
})(jQuery);
