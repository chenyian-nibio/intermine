<!-- dataCategories -->

        <link href="http://cdn.intermine.org/css/bootstrap/2.0.3-prefixed/css/bootstrap.min.css" rel="stylesheet" />
        <link href="http://cdn.intermine.org/js/intermine/im-tables/latest/tables.css" rel="stylesheet" />
        <link href="http://cdn.intermine.org/css/jquery-ui/1.8.19/jquery-ui-1.8.19.custom.css" rel="stylesheet" />
        <link href="http://cdn.intermine.org/css/google-code-prettify/latest/prettify.css" rel="stylesheet" />
        <link href="http://cdn.intermine.org/css/font-awesome/css/font-awesome.css" rel="stylesheet" />
		<style type="text/css">
			.im-query-actions { display: none; }
			.im-management-tools { display: none; }
		</style>
        
        <script type="text/javascript" src="/targetmine/vendor/jquery.js"></script>
        <script type="text/javascript" src="/targetmine/vendor/underscore.js"></script>
        <script type="text/javascript" src="/targetmine/vendor/backbone.js"></script>
        <script type="text/javascript" src="/targetmine/vendor/imjs.js"></script>
        <script type="text/javascript" src="/targetmine/vendor/imtables.deps.js"></script>

        <script src="/targetmine/vendor/imtables.js"></script>

        <script type="text/javascript">
        $(function() {
            var pq, service, view;
            pq = {
                model: {
                    name: "genomic"
                },
                select: ["DataSet.category", "DataSet.name", "DataSet.version", "DataSet.dateType", "DataSet.date", "DataSet.description"]
            };
            
            service = new intermine.Service({
                root: "${WEB_PROPERTIES['project.sitePrefix']}/service/",
            });
            
            view = new intermine.query.results.CompactView(service, pq);
            
            view.$el.appendTo('#data-source-table');
            
            return view.render();
        });
        </script>
    
		<div class="body">
			
			<div id="data-source-table"> </div>
		</div>

<!-- /dataCategories -->
