this.addScript('json.js',["stringifyJSON","parseJSON"]);

this.addScript('uri.js',["URI","buildURIMatcher"]);
this.addScript('kv.js',["setByKey","getByKey","removeByKey"]);

this.addScript('resource.js',['loadXML','selectNodes','NodeList']
                ,0
                ,['org.xidea.jsi:$log','org.xidea.jsidoc.util:XMLHttpRequest']);
