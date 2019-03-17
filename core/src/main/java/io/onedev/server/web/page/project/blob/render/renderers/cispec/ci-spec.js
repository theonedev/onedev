onedev.ciSpec = {
    edit: {
        showJob: function(index) {
            var $body = $(".ci-spec-edit .jobs>.body");
            var $navs = $body.find(">.side>.navs");
            $navs.children().removeClass("active");
            $navs.children().eq(index).addClass("active");
            var $contents = $body.children(".contents");
            $contents.children().hide();
            $contents.children().eq(index).show();
        },
        deleteJob: function(index) {
            var $body = $(".ci-spec-edit .jobs>.body");
            var $navs = $body.find(">.side>.navs");
            var $contents = $body.children(".contents");
            var $nav = $navs.children().eq(index);
            $nav.remove();
            $contents.children().eq(index).remove();

            if ($nav.hasClass("active")) {
                var $nextNav = $navs.children().eq(index);
                if ($nextNav.hasClass("nav")) {
                    $nextNav.addClass("active");
                    $contents.children().eq(index).show();
                }
            } 
        }, 
        swapJobs: function(index1, index2) {
            var $contents = $(".ci-spec-edit .jobs>.body>.contents");

            if (index1 < index2) {
                for (var i = 0; i < index2-index1; i++) 
                    $contents.children().eq(index1+i).before($contents.children().eq(index1+i+1));
            } else {
                for (var i = 0; i < index1-index2; i++) 
                    $contents.children().eq(index1-i).after($contents.children().eq(index1-i-1));
            }
        },
        trackJobNameChange: function(index) {
            var $body = $(".ci-spec-edit .jobs>.body");
            var $navs = $body.find(">.side>.navs");
            var $nav = $navs.children().eq(index);
            var $contents = $body.children(".contents");
            var $content = $contents.children().eq(index);
            
            var $input = $content.find(">div>table>tbody>tr>td.property-name input");
            $input.on("input", function() {
                var name = $input.val().trim();
                var $name = $nav.find("a.select>.name");
                if (name.length != 0) 
                    $name.text(name);
                else
                    $name.html("<i>Adding new</i>");
            });
        }
    }
}