package mflux.plugin.test.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveShareUrlCreate extends PluginService {

    public static final String SERVICE_NAME = "my.test.archive.share.url.create";

    private Interface _defn;

    public SvcArchiveShareUrlCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "The query to find all the assets to be included in the archive.",
                1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Create a sharable url to create and download the archive that contains the matching assets specified by the where query argument.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String where = args.value("where");
        String token = createToken(executor(), where);
        w.add("url", createUrl(executor(), token));
    }

    private static String createUrl(ServiceExecutor executor, String token)
            throws Throwable {
        XmlDoc.Element se = executor.execute("system.session.self.describe")
                .element("session");
        String host = se.value("host");
        XmlDoc.Element re = executor.execute("network.describe",
                "<args><type>http</type></args>", null, null);
        int port = re.intValue("service/@port");
        boolean ssl = re.booleanValue("service/@ssl");
        StringBuilder sb = new StringBuilder(ssl ? "https://" : "http://");
        sb.append(host);
        if ((ssl && port != 443) || (!ssl && port != 80)) {
            sb.append(":").append(port);
        }
        sb.append("/mflux/execute.mfjp?token=").append(token)
                .append("&filename=")
                .append(new SimpleDateFormat("yyyyMMddHHmmssSSS")
                        .format(new Date()))
                .append(".zip");
        return sb.toString();
    }

    private static String createToken(ServiceExecutor executor, String where)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("role", new String[] { "type", "user" }, arc.mf.plugin.Session.user().domain() 
                 + ":" + arc.mf.plugin.Session.user().name());
        dm.push("service",
                new String[] { "name", SvcArchiveCreate.SERVICE_NAME });
        dm.add("where", where);
        dm.pop();
        dm.add("min-token-length", 20);
        dm.add("max-token-length", 20);
        dm.add("grant-caller-transient-roles", true);
        dm.add("to", new Date(new Date().getTime() + 3L * 86400000L));
        dm.add("tag", "mflux-test-share-url-"
                + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        return executor.execute("secure.identity.token.create", dm.root())
                .value("token");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
