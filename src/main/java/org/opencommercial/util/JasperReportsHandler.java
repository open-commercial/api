package org.opencommercial.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.exception.ServiceException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

@Component
public class JasperReportsHandler {

  private final MessageSource messageSource;

  public JasperReportsHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public byte[] compilar(String pathToJrxml, Map<String, Object> params, Collection<?> datasource, FormatoReporte formato) {
    JasperReport jasperReport;
    JasperPrint jasperPrint;
    var ds = new JRBeanCollectionDataSource(datasource);
    try {
      var classLoader = this.getClass().getClassLoader();
      var isFileReport = classLoader.getResourceAsStream(pathToJrxml);
      jasperReport = JasperCompileManager.compileReport(isFileReport);
      jasperPrint = JasperFillManager.fillReport(jasperReport, params, ds);
      return switch (formato) {
        case PDF -> JasperExportManager.exportReportToPdf(jasperPrint);
        case XLSX -> convertirAlFormatoXlsx(jasperPrint);
        default -> throw new BusinessServiceException(
                messageSource.getMessage("mensaje_formato_no_valido", null, Locale.getDefault()));
      };
    } catch (JRException | IOException ex) {
      throw new ServiceException(messageSource.getMessage("mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }

  private byte[] convertirAlFormatoXlsx(JasperPrint jasperPrint) throws JRException, IOException {
    var jasperXlsxExportMgr = new JRXlsxExporter();
    var out = new ByteArrayOutputStream();
    var simpleOutputStreamExporterOutput = new SimpleOutputStreamExporterOutput(out);
    jasperXlsxExportMgr.setExporterInput(new SimpleExporterInput(jasperPrint));
    jasperXlsxExportMgr.setExporterOutput(simpleOutputStreamExporterOutput);
    jasperXlsxExportMgr.exportReport();
    byte[] bytes = out.toByteArray();
    out.close();
    return bytes;
  }
}
