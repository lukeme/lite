this.addScript('template.js',["Template","PLUGIN_DEFINE",'VAR_TYPE','XML_ATTRIBUTE_TYPE','ELSE_TYPE','PLUGIN_TYPE','CAPTRUE_TYPE','IF_TYPE','EL_TYPE','BREAK_TYPE','XML_TEXT_TYPE','FOR_TYPE']
                ,0
                ,['Translator','XMLParser','org.xidea.jsi:$log','org.xidea.el:evaluate']);

this.addScript('js-translator.js',["Translator"]
                ,0
                ,["org.xidea.el:ELTranslator",'VarStatus','org.xidea.jsi:$log']);

this.addScript('variable-finder.js','VarStatus'
                ,0
                ,["org.xidea.el:evaluate","org.xidea.el:ELTranslator",'org.xidea.jsi:$log']);
                
this.addDependence("*",'*',true);