// Google Apps Script Code for JewelEase Form Submission
// Deploy this as a Web App with "Execute as: Me" and "Access: Anyone"

function doPost(e) {
  try {
    // Log the incoming request for debugging
    console.log('Received POST request');
    console.log('Event object:', JSON.stringify(e));
    console.log('Parameters:', JSON.stringify(e.parameter));
    
    // Your Google Sheets ID
    const spreadsheetId = '1WRzzqQbl0ivuSFrwTNFMdsYYlzXefM15rv36OIY706o';
    
    // Try to open the spreadsheet
    let spreadsheet;
    try {
      spreadsheet = SpreadsheetApp.openById(spreadsheetId);
    } catch (sheetError) {
      console.error('Cannot access spreadsheet:', sheetError);
      throw new Error('Spreadsheet not accessible. Please check the ID and permissions.');
    }
    
    const sheet = spreadsheet.getSheetByName('Sheet1'); // Use your actual sheet name
    if (!sheet) throw new Error('Sheet not found');
    
    // Parse the form data
    const formData = e.parameter;
    
    // Validate required fields
    if (!formData.fullName || !formData.email) {
      throw new Error('Missing required fields: fullName and email are mandatory');
    }
    
    // Create timestamp in Indian timezone
    const indianTime = new Date().toLocaleString('en-IN', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    
    // Prepare the data to be inserted
    const rowData = [
      indianTime, // Timestamp
      formData.fullName || '',
      formData.email || '',
      formData.phone || '',
      formData.storeName || '', // Store Name
      formData.message || '',
      'New' // Status column
    ];
    
    // Check if this is the first entry (add headers)
    if (sheet.getLastRow() < 1 || sheet.getRange(1, 1).getValue() === '') {
      const headers = ['Timestamp', 'Full Name', 'Email', 'Phone', 'Store Name', 'Message', 'Status'];
      sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
      
      // Format the header row
      const headerRange = sheet.getRange(1, 1, 1, headers.length);
      headerRange.setBackground('#D4AF37');
      headerRange.setFontColor('#1C1F33');
      headerRange.setFontWeight('bold');
      
      console.log('Headers added to sheet');
    }
    
    // Add the new row
    sheet.appendRow(rowData);
    console.log('Data added to sheet:', rowData);
    
    // Auto-resize columns for better readability
    sheet.autoResizeColumns(1, 7);
    
    // Send email notification to admin
    try {
      sendEmailNotification(formData, indianTime);
    } catch (emailError) {
      console.error('Email notification failed:', emailError);
      // Don't fail the entire process if email fails
    }
    
    // Return success response
    const response = {
      status: 'success',
      message: 'Form submitted successfully',
      timestamp: indianTime
    };
    
    console.log('Returning success response:', response);
    
    return ContentService
      .createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (error) {
    console.error('Error in doPost:', error);
    
    // Return detailed error response
    const errorResponse = {
      status: 'error',
      message: error.message || 'Unknown error occurred',
      timestamp: new Date().toLocaleString('en-IN', {timeZone: 'Asia/Kolkata'})
    };
    
    return ContentService
      .createTextOutput(JSON.stringify(errorResponse))
      .setMimeType(ContentService.MimeType.JSON);
  }
}


function doGet(e) {
  // Handle GET requests (for testing and health checks)
  const response = {
    status: 'running',
    message: 'JewelEase Form Handler is active',
    timestamp: new Date().toLocaleString('en-IN', {timeZone: 'Asia/Kolkata'}),
    version: '1.0'
  };
  
  console.log('GET request received, returning:', response);
  
  return ContentService
    .createTextOutput(JSON.stringify(response))
    .setMimeType(ContentService.MimeType.JSON);
}


function sendEmailNotification(formData, timestamp) {
  try {
    // Your admin email - UPDATE THIS if needed
    const adminEmail = 'veerarts8605@gmail.com';
    
    const subject = '🔔 New Demo Request - JewelEase';
    
    const body = `
New demo request received from JewelEase website!

📋 Customer Details:
━━━━━━━━━━━━━━━━━━━━━━━━━━
👤 Name: ${formData.fullName}
📧 Email: ${formData.email}
📱 Phone: ${formData.phone}
🏪 Store Name: ${formData.storeName || 'Not provided'}
💬 Message: ${formData.message || 'No message provided'}

⏰ Submitted: ${timestamp}

🎯 Action Required:
Please follow up within 24 hours to schedule the demo.

━━━━━━━━━━━━━━━━━━━━━━━━━━
JewelEase CRM System
Automated Lead Notification
    `;
    
    MailApp.sendEmail({
      to: adminEmail,
      subject: subject,
      body: body
    });
    
    console.log('Email notification sent successfully to:', adminEmail);
    
  } catch (error) {
    console.error('Failed to send email notification:', error);
    throw error; // Re-throw to be caught by caller
  }
}
