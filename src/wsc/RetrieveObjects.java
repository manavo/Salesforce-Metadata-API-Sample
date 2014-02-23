package wsc;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.metadata.ValidationRule;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

public class RetrieveObjects {

	// Currently just supports a single object for testing
	public static String sourceObject = "Account";
	
	static PartnerConnection connection;

	/* Helpful reference code:
	 * 		- http://salesforce.stackexchange.com/questions/22003/parsing-metadata-api-xml 
	 */
	public static void main(String[] args) throws ConnectionException, InterruptedException, IOException, PullParserException {
	  
	    ConnectorConfig partnerConfig = new ConnectorConfig();
	    ConnectorConfig metadataConfig = new ConnectorConfig();
	    
	    loadCredentials(partnerConfig);
	    //partnerConfig.setTraceMessage(true);
	    
	    @SuppressWarnings("unused")
	    PartnerConnection partnerConnection = com.sforce.soap.partner.Connector.newConnection(partnerConfig);
	    
	    // shove the partner's session id into the metadata configuration then connect
	    metadataConfig.setSessionId(partnerConnection.getSessionHeader().getSessionId());
	    MetadataConnection metadataConnection = com.sforce.soap.metadata.Connector.newConnection(metadataConfig);
	    
		// Retrieve Custom Object Meta data for Source Object
		RetrieveRequest retrieveRequest = new RetrieveRequest();
		retrieveRequest.setSinglePackage(true);
		com.sforce.soap.metadata.Package packageManifest = new com.sforce.soap.metadata.Package();
		ArrayList<PackageTypeMembers> types = new ArrayList<PackageTypeMembers>();
		PackageTypeMembers packageTypeMember = new PackageTypeMembers();
		packageTypeMember.setName("CustomObject");
		packageTypeMember.setMembers(new String[] { sourceObject });
		types.add(packageTypeMember);
		packageManifest.setTypes((PackageTypeMembers[]) types.toArray(new PackageTypeMembers[] {}));
		retrieveRequest.setUnpackaged(packageManifest);
		AsyncResult response = metadataConnection.retrieve(retrieveRequest);
		while(!response.isDone())
		{
		    Thread.sleep(1000);
		    response = metadataConnection.checkStatus(new String[] { response.getId()} )[0];
		}
		RetrieveResult retrieveResult = metadataConnection.checkRetrieveStatus(response.getId());

		// Parse Custom Object Meta Data for Source Object
		CustomObject customObject = new CustomObject();
		byte[] zipBytes = retrieveResult.getZipFile();
		ZipInputStream zipis = new ZipInputStream(new ByteArrayInputStream(zipBytes, 0, zipBytes.length));
		ZipEntry zipEntry = null;
		while((zipEntry = zipis.getNextEntry()) != null)
		{
		    if(zipEntry.getName().endsWith(sourceObject + ".object"))
		    {
		        TypeMapper typeMapper = new TypeMapper();
		        XmlInputStream xmlis = new XmlInputStream();
		        xmlis.setInput(zipis, "UTF-8");
		        customObject.load(xmlis, typeMapper);
		        zipis.closeEntry();
		        break;
		    }
		}
		
		System.out.println("Object...\n========================");
		System.out.println("Label: " + customObject.getLabel());
		System.out.println("");
		
		System.out.println("Validation Rules...\n========================");
		for (ValidationRule rule : customObject.getValidationRules())
		{
			System.out.println("Name: " + rule.getFullName());
			System.out.println("Formula: " + rule.getErrorConditionFormula());
			System.out.println("Error Message: " + rule.getErrorMessage());
			System.out.println("");
		}
		
		System.out.println("Fields...\n========================");
		for (CustomField field : customObject.getFields())
		{
			System.out.println("Label: " + field.getLabel());
			System.out.println("Name: " + field.getFullName());
			System.out.println("Type: " + field.getType().toString());
			System.out.println("");
		}
	}
	
	public static void loadCredentials(ConnectorConfig partnerConfig) throws IOException
	{
		Properties prop = new Properties();
		InputStream input = null;
		
		try
		{
			input = new FileInputStream("config.properties");
			prop.load(input);

		    // TODO - allow custom auth endpoints (sandbox instead of production)
		    partnerConfig.setUsername(prop.getProperty("username"));
		    partnerConfig.setPassword(prop.getProperty("password"));
		}
		catch (IOException e)
		{
			if (input != null)
			{
				input.close();
			}
			throw e;
		}
	}
}
