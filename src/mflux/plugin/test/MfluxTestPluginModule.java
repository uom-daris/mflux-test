package mflux.plugin.test;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import mflux.plugin.test.services.SvcArchiveCreate;
import mflux.plugin.test.services.SvcArchiveShareUrlCreate;

public class MfluxTestPluginModule implements PluginModule {

    private List<PluginService> _services;

    public MfluxTestPluginModule() {
        _services = new Vector<PluginService>();
        _services.add(new SvcArchiveCreate());
        _services.add(new SvcArchiveShareUrlCreate());
    }

    @Override
    public String description() {
        return "A plugin module to prototype/test plugin services.";
    }

    @Override
    public void initialize(ConfigurationResolver config) throws Throwable {

    }

    @Override
    public Collection<PluginService> services() {
        return _services;
    }

    @Override
    public void shutdown(ConfigurationResolver arg0) throws Throwable {

    }

    @Override
    public String vendor() {
        return "Wilson Liu";
    }

    @Override
    public String version() {
        return "0.0.1";
    }

}
