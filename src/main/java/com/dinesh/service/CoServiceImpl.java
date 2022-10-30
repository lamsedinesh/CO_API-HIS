package com.dinesh.service;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dinesh.entity.CitizenAppEntity;
import com.dinesh.entity.CoTriggerEntity;
import com.dinesh.entity.DcCaseEntity;
import com.dinesh.entity.EligibilityDtls;
import com.dinesh.repository.CitizenAppRepository;
import com.dinesh.repository.CoTriggerRepository;
import com.dinesh.repository.DcCaseRepo;
import com.dinesh.repository.EligDtlsRepository;
import com.dinesh.utils.EmailUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


@Service
public class CoServiceImpl implements CoService {
	@Autowired
	private EligDtlsRepository eligRepo;

	@Autowired
	private CoTriggerRepository triggerRepo;
	
	@Autowired
	private EmailUtils emailUtils;
	@Autowired
	private DcCaseRepo casesRepo;
	@Autowired
	private CitizenAppRepository appRepo;
	FileInputStream pdfFis;
	File file;

	@Override
	public String processCoTriggers() throws SerialException, SQLException, IOException {

		List<CoTriggerEntity> pendingTriggers = triggerRepo.findByTriggerStatus("Pending");
		int recordCnt = 0;
		for (CoTriggerEntity trigger : pendingTriggers) {
			EligibilityDtls eligDtls = eligRepo.findByCaseNo(trigger.getCaseNum());
		 
				
				Optional<DcCaseEntity> caseEntiOptional = casesRepo.findByCaseNo(eligDtls.getCaseNum());
				DcCaseEntity dcCase = caseEntiOptional.get();
				Optional<CitizenAppEntity> appEntity = appRepo.findById(dcCase.getAppId());
				CitizenAppEntity citizenAppEntity = appEntity.get();
				
				//generate pdf
				generatePdf(eligDtls);
				
				//mail logic
				String mailBody = "Dear" + citizenAppEntity.getFullName() + ",PFA plan details";

				boolean isMailSent = emailUtils.sendEmail(citizenAppEntity.getEmail(), "HIS Pland Details", mailBody,
						pdfFis);
				if (isMailSent) {
					boolean isTriggerUpdated = updateCoTriggerRecord(trigger, pdfFis);
					if(isTriggerUpdated) {
						pdfFis.close();
						file.delete();
						recordCnt++;
					}
					
				}
			}
		
		if (recordCnt > 0) {
			return recordCnt + " triggers processed succesfully";
		} else {
			return "No trigger is in pending status";
		}
	}

	@Override
	public void generatePdf(EligibilityDtls dtls) {

		Document document = new Document(PageSize.A4);
		try {
			file = new File("CoTrigger" + dtls.getEligId() + ".pdf");
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			// PdfWriter.getInstance(document, response.getOutputStream());
			PdfWriter.getInstance(document, fileOutputStream);
			document.open();
			Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
			font.setSize(18);
			font.setColor(Color.BLUE);

			Paragraph p = createPpdfPara(font);

			PdfPTable table = createPdfTable();

			writePdfHeader(table);

			writePdfData(dtls, table);

			document.add(p);
			document.add(table);
			document.close();
			
			pdfFis=new FileInputStream(file);
			fileOutputStream.close();
			
			
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private PdfPTable createPdfTable() {
		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(100f);
		table.setWidths(new float[] { 3.5f, 1.5f, 1.5f, 3.0f, 3.0f, 1.5f, 3.0f });
		table.setSpacingBefore(10);
		return table;
	}

	private Paragraph createPpdfPara(Font font) {
		Paragraph p = new Paragraph("Correspondence Notice", font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		return p;
	}

	private void writePdfData(EligibilityDtls dtls, PdfPTable table) {

		table.addCell(dtls.getHoldersName());
		table.addCell(dtls.getPlanName());
		table.addCell(dtls.getPlanStatus());
		table.addCell(dtls.getPlanStartDate().toString());
		table.addCell(dtls.getPlanEndDate().toString());
		table.addCell(String.valueOf(dtls.getBenefitAmt()));
		table.addCell(dtls.getDenielReason());
	}

	private void writePdfHeader(PdfPTable table) {
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(5);

		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA);
		headerFont.setColor(Color.WHITE);

		cell.setPhrase(new Phrase("citizen Name", headerFont));

		table.addCell(cell);

		cell.setPhrase(new Phrase("Plan Name", headerFont));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Plan Status", headerFont));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Plan Start Date", headerFont));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Plan End Date", headerFont));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Benefit Amount", headerFont));
		table.addCell(cell);

		cell.setPhrase(new Phrase("Deniel Reason", headerFont));
		table.addCell(cell);
	}

	@Override
	public boolean  updateCoTriggerRecord(CoTriggerEntity triggerEntity, FileInputStream fis) {
		try {
			//byte[] org.apache.commons.io.IOUtils.toByteArray(InputStream fis);
	       // InputStream cont = new ByteArrayInputStream(IOUtils.toByteArray(fis));
			Blob fileBolb = new SerialBlob(IOUtils.toByteArray(fis));
			triggerEntity.setFileData(fileBolb);
			triggerEntity.setTrgStatus("Processed");
			triggerRepo.save(triggerEntity);
			return true;

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		return false;



	

}
}
