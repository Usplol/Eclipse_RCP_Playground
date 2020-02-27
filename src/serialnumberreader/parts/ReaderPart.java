package serialnumberreader.parts;

import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;

public class ReaderPart {

	@Inject
	private MPart part;
	Text serialNumberInputTxt;
	Label dateTimeLabel;
	Label workstationIdLabel;
	private Button btnCheckButton;

	@PostConstruct
	public void createComposite(Composite parent) {
		GridLayout gl_parent = new GridLayout(2, false);
		gl_parent.marginWidth = 10;
		parent.setLayout(gl_parent);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);

		serialNumberInputTxt = new Text(parent, SWT.BORDER);
		serialNumberInputTxt.setMessage("Enter Serial Number");
		serialNumberInputTxt.addVerifyListener(new VerifyListener() {  
		    @Override  
		    public void verifyText(VerifyEvent e) {
		        /* Notice how we combine the old and new below */
		        String currentText = ((Text)e.widget).getText();
		        String port =  currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
		        try{  
		            long portNum = Long.parseLong(port);  
		            if(portNum < 0 || portNum > 0xFFFFFFFFL ){  
		                e.doit = false;  
		            }  
		        }  
		        catch(NumberFormatException ex){  
		            if(!port.equals(""))
		                e.doit = false;  
		        }  
		    }  
		});
		
		
		
		//txtInput.addModifyListener(e -> part.setDirty(true));
		GridData gd_txtInput = new GridData(GridData.FILL_HORIZONTAL);
		gd_txtInput.horizontalSpan = 2;
		gd_txtInput.widthHint = 431;
		gd_txtInput.verticalAlignment = SWT.FILL;
		serialNumberInputTxt.setLayoutData(gd_txtInput);
		
		dateTimeLabel = new Label(parent, SWT.BORDER);
		dateTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		dateTimeLabel.setText("Date/Time");
		
		workstationIdLabel = new Label(parent, SWT.BORDER);
		GridData gd_workstationIdLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_workstationIdLabel.widthHint = 297;
		workstationIdLabel.setLayoutData(gd_workstationIdLabel);
		workstationIdLabel.setText("Work Station ID:    ");
		
		btnCheckButton = new Button(parent, SWT.CHECK);
		btnCheckButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnCheckButton.setText("Old Product");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Button btnNewButton = new Button(parent, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
		gd_btnNewButton.widthHint = 101;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Read");
		btnNewButton.addSelectionListener(new SelectionListener() {
			
			private long serialNumber = 0;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try
			    {
			        // the String to int conversion happens here
					serialNumber = Long.parseLong(serialNumberInputTxt.getText().trim());
			    }
			    catch (NumberFormatException nfe)
			    {
			      System.out.println("NumberFormatException: " + nfe.getMessage());
			    }
				
				if (btnCheckButton.getSelection()) { 
					// old product, 32 bit date/time
					dateTimeLabel.setText(getDateTimeOld(serialNumber));
					workstationIdLabel.setText("Workstation ID: n/a");
				} else {
					// product after 2015. 29 bit date/time
					dateTimeLabel.setText(getDateTime(serialNumber));
					workstationIdLabel.setText(getWorkStationID(serialNumber));
				}
				
							
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}


	@Persist
	public void save() {
		//part.setDirty(false);
	}
	
	private String getDateTime(long serialNumber) {
		// 32 bits serial number = 3 bits panel ID + 29 bits Unix timestamp starting from 01/01/2014 00:00:00
		// So 8 unique panels ID and unique timestamp for 17 years. 
		// Unix timestamp from 01/01/1970 00:00:00 till 01/01/2014 00:00:00 is 1388534400
		int seconds = (int)(serialNumber & 0x1FFFFFFF);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 0, 1, 0, 0, 0);
		calendar.add(Calendar.SECOND, seconds);
		
		
		
		return "Date/Time: " + calendar.getTime();
		
	}
	
	private String getDateTimeOld(long serialNumber) {
		// 32 bits serial number is Unix timestamp starting from 01/01/1970 00:00:00
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 0, 1, 0, 0, 0);
		if (serialNumber <= Integer.MAX_VALUE) {
			calendar.add(Calendar.SECOND, (int)serialNumber);
		} else {
			calendar.add(Calendar.SECOND, Integer.MAX_VALUE);
			calendar.add(Calendar.SECOND, (int)(serialNumber - Integer.MAX_VALUE));
		}
		
		
		
		return "Date/Time: " + calendar.getTime();
		
	}
	
	private String getWorkStationID(long serialNumber) {
		int id = (int)((serialNumber & 0xE0000000) >> 29);
		return "Workstation ID: " + id;
	}
	

}