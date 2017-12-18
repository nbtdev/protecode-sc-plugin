/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synopsys.protecode.sc.jenkins;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.synopsys.protecode.sc.jenkins.types.InternalTypes.*;
import com.synopsys.protecode.sc.jenkins.types.InternalTypes.FileAndResult;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author pajunen
 */
public class ReportBuilder {
    
    private static final String PROTECODE_FILE_TAG = "protecodesc";
    
    public static boolean report(
        List<FileAndResult> results, 
        AbstractBuild<?, ?> build, 
        BuildListener listener,
        String reportDirectory
    ) {
        
        if (!Utils.makeDirectory(build, reportDirectory, listener)) {
            listener.error("Couldn't create report directory.");
            return false;
        }
        FilePath jsonReportDirectory = build.getWorkspace().child(reportDirectory);
        
        PrintStream log = listener.getLogger();
        ObjectMapper mapper = getObjectMapper();
        results.forEach((result) -> {
            writeJson(log, mapper, jsonReportDirectory, result.getSerializableResult());
        });
        
        return true;
    }   
    
    public static boolean makeSummary(
        List<FileAndResult> results, 
        AbstractBuild<?, ?> build, 
        BuildListener listener,
        String reportDirectory
    ) throws IOException, InterruptedException {
        @SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        FilePath jsonReportDirectory = build.getWorkspace().child(reportDirectory);
        
        PrintStream log = listener.getLogger();
        
        log.println("Creating xml for summary plugin");
        ObjectMapper mapper = getObjectMapper();
        try {
            FilePath[] jsonFiles = jsonReportDirectory.list("*-" + PROTECODE_FILE_TAG + ".json");
            log.println(jsonFiles.length + " files found");
            File xmlReportDir = build.getArtifactsDir();
            if (!xmlReportDir.exists()) {
                boolean xmlReportDirCreated = xmlReportDir.mkdirs();
                if (!xmlReportDirCreated) {
                    log.println("XML report directory could not be created.");
                    throw new IOException("XML report directory could not be created.");
                }
            }
            File xmlFile = new File(xmlReportDir, PROTECODE_FILE_TAG + ".xml");

            log.println("Creating xml report to " + xmlFile.getName());

            OutputStream out = new BufferedOutputStream(
                    new FileOutputStream(xmlFile));
            createXmlReport(jsonFiles, mapper, out);
            out.close();
        } catch (NullPointerException e) {
            // NOP
        }
        return true;
    }

    static void createXmlReport(final FilePath[] jsonFiles, final ObjectMapper mapper,
            OutputStream xmlFile) throws IOException, InterruptedException {

        PrintStream out = new PrintStream(xmlFile, false, "UTF-8");
        out.println(
                "<section name=\"Protecode SC analysis result\" fontcolor=\"#000000\">");
        for (FilePath jsonFile : jsonFiles) {
            try (InputStream in = new BufferedInputStream(jsonFile.read())) {
                SerializableResult readResult = mapper.readValue(in, SerializableResult.class);
                Long exact = readResult.getResults().getSummary().getVulnCount()
                        .getExact();
                String verdict = readResult.getResults().getSummary().getVerdict()
                        .getShortDesc();
                String verdict_detailed = readResult.getResults().getSummary().getVerdict()
                        .getDetailed();
                out.println("<accordion name =\"" + readResult.getFilename()
                        + " (" + verdict + ")\">");

                Color color = exact > 0L ? Color.RED : Color.GREEN;
                writeField(out, "Verdict", verdict_detailed, color);
                writeField(out, "Vulnerabilities", exact.toString(), Color.BLACK);
                writeField(out, "Report", "", Color.BLACK,
                                "<a target=\"_blank\" href=\""
                                + readResult.getResults().getReport_url()
                                + "\">View full report in Protecode SC </a>");
                out.println("</accordion>");
            }
        }
        out.println("</section>");
        out.close();
    }
   
    private static enum Color {
        RED("#ff0000"), GREEN("#00ff00"), YELLOW("#ff9c00"), BLACK("#000000");

        private String value;

        private Color(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    private static void writeField(PrintStream out, String name, String value,
            Color valueColor) {
        writeField(out, name, value, valueColor, null);
    }

    private static void writeField(PrintStream out, String name, String value,
            Color valueColor, String cdata) {
        out.append("<field name=\"" + name + "\" titlecolor=\"black\" value=\""
                + value + "\" ");
        out.append("detailcolor=\"" + valueColor.getValue() + "\">\n");
        if (cdata != null && !cdata.isEmpty()) {
            out.print("<![CDATA[");
            out.print(cdata);
            out.print("]]>");
        }
        out.append("</field>\n");
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }

    private static void writeJson(PrintStream log, ObjectMapper mapper,
            FilePath workspaceJsonReportDirectory, SerializableResult result) {
        if (result.getResults() == null) {
            log.println("No scan result for file: " + result.getFilename());
            return;
        }
        FilePath jsonFile = workspaceJsonReportDirectory.child(
            result.getFilename()+ "-" + PROTECODE_FILE_TAG + ".json"
        );

        try (OutputStream out = new BufferedOutputStream(jsonFile.write())) {
            mapper.writeValue(out, result.getResults());
        } catch (IOException | InterruptedException e) {
            log.println(e.toString());
        }
    }
}
