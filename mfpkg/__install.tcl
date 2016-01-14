set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        mflux-test-plugin.zip
set plugin_jar        mflux-test-plugin.jar
set module_class      mflux.plugin.test.MfluxTestPluginModule

# extract mflux-test-plugin.jar to /mflux/plugins
asset.import :url archive:${plugin_zip} \
		:namespace -create yes ${plugin_namespace} \
		:label -create yes ${plugin_label} :label PUBLISHED \
        :update true

# install the plugin module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "false" } {
		plugin.module.add :path ${plugin_namespace}/${plugin_jar} :class ${module_class}
}

# reload the services     
system.service.reload

# refresh the enclosing shell
srefresh
