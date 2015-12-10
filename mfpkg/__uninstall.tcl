set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        mflux-test-plugin.zip
set plugin_jar        mflux-test-plugin.jar
set module_class      mflux.test.MfluxTestPluginModule

if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "true" } {
	plugin.module.remove :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

if { [xvalue exists [asset.exists :id name=${plugin_namespace}/${plugin_jar}]] == "true" } {
   	asset.destroy :id name=${plugin_namespace}/${plugin_jar}
}

system.service.reload

srefresh
