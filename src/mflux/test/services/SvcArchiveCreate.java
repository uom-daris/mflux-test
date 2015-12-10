package mflux.test.services;

import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;

import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcArchiveCreate extends PluginService {

    public static final String SERVICE_NAME = "my.test.archive.create";

    private Interface _defn;

    public SvcArchiveCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "The query to find all the assets to be included in the archive.",
                1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "A test service to create a zip archive that includes the metadata and content of the matching assets.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String where = args.value("where");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", where);
        dm.add("size", "infinity");
        final Collection<String> assetIds = executor()
                .execute("asset.query", dm.root()).values("id");
        if (assetIds == null || assetIds.isEmpty()) {
            throw new Exception("No matching asset found.");
        }

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
            @Override
            public void run() {
                try {
                    int totalObjects = assetIds.size();

                    try {
                        PluginTask.threadTaskBeginSetOf(totalObjects);
                        ArchiveOutput ao = ArchiveRegistry.createOutput(pos,
                                "application/zip", 6, null);
                        try {
                            for (String assetId : assetIds) {
                                PluginTask.checkIfThreadTaskAborted();
                                PluginTask.setCurrentThreadActivity(
                                        "Processing asset " + assetId);
                                addToArchive(executor(), assetId, ao);
                                PluginTask.clearCurrentThreadActivity();
                                PluginTask
                                        .threadTaskCompletedOneOf(totalObjects);
                            }
                        } finally {
                            ao.close();
                        }
                        PluginTask.threadTaskCompleted();
                    } finally {
                        pos.close();
                        pis.close();
                    }
                } catch (Throwable e) {
                    e.printStackTrace(System.out);
                }
            }

        });
        outputs.output(0).setData(pis, -1, "application/zip");
    }

    private static void addToArchive(ServiceExecutor executor, String assetId,
            ArchiveOutput ao) throws Throwable {
        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + assetId + "</id></args>", null, null)
                .element("asset");
        addMetaToArchive(ae, ao);
        if (ae.elementExists("content")) {
            addContentToArchive(executor, ae, ao);
        }
    }

    private static void addContentToArchive(ServiceExecutor executor,
            Element ae, ArchiveOutput ao) throws Throwable {
        String assetId = ae.value("@id");
        String ctype = ae.value("content/type");
        String ext = ae.value("content/type/@ext");
        long csize = ae.longValue("content/size");

        String entryName = assetId;
        if (ext != null) {
            entryName = assetId + "." + ext;
        }
        PluginService.Outputs outputs = new PluginService.Outputs(1);
        executor.execute("asset.content.get",
                "<args><id>" + assetId + "</id></args>", null, outputs);
        PluginService.Output output = outputs.output(0);
        try {
            ao.add(ctype, entryName, output.stream(), csize);
        } finally {
            output.stream().close();
            output.close();
        }
    }

    private static void addMetaToArchive(XmlDoc.Element ae, ArchiveOutput ao)
            throws Throwable {
        String assetId = ae.value("@id");
        byte[] bytes = ae.toString().getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            ao.add("text/xml", assetId + ".meta.xml", is, bytes.length);
        } finally {
            is.close();
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

}
