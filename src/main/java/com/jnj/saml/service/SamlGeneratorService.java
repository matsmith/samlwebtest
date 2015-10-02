package com.jnj.saml.service;

import com.mifmif.common.regex.Generex;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.*;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class SamlGeneratorService {

    private final Logger log = LoggerFactory.getLogger(SamlGeneratorService.class);
    private String password = "hws1234@@"; //"password";
    private String certificateAliasName = "healthmedia.net"; //"selfsigned";
    private String certificateFileName = "healthmedia.net.jks";//"/Users/manojsingh/jnj/keystore.jks";
    private String customerId = null;
    private String resourceUrl = null;
    public String ASSERTION_ID_REGEX = "_[a-z0-9]{32}";



    public SAMLAssertionData populateAssertionData(){
        //its all hardcoded for now
        SAMLAssertionData assertionData = new SAMLAssertionData();
        assertionData.setStrIssuer("https://idp.healthmedia.com/idp/shibboleth");
        assertionData.setStrNameID("_059b0e22b166b9ae7e775c9beee3616d");
        assertionData.setStrNameQualifier("https://idp.healthmedia.com/idp/shibboleth");
        assertionData.setStrSpNameQualifier(customerId);//sp_hmi_idp_dpdevcr03
        assertionData.setSessionId("abcdedf1234567");

        Map customAttributes = new HashMap<String, String>();
        customAttributes.put("locale","en_US");
        customAttributes.put("lastname", "Singh");
        customAttributes.put("birthdate","12/19/1976");
        customAttributes.put("gender", "Male");
        customAttributes.put("accesscd", "HMITESTCD");
        customAttributes.put("umemberid","dmtestsso");
        customAttributes.put("firstname", "Chris");
        customAttributes.put("primaryemail", "msingh77@its.jnj.com");

        assertionData.setAttributes(customAttributes);

        return assertionData;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String url) {
        this.resourceUrl = url;
    }

    public String getCustomerId() {
        return resourceUrl;
    }

    public void setCustomerId(String id) {
        this.customerId = id;
    }

    public Assertion generateSamlResponse(SAMLAssertionData samlAssertionData, DateTime now) {
        try {

            // Create the NameIdentifier
            SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(NameID.DEFAULT_ELEMENT_NAME);
            NameID nameId = (NameID) nameIdBuilder.buildObject();
            nameId.setValue(samlAssertionData.getStrNameID());
            nameId.setNameQualifier(samlAssertionData.getStrNameQualifier());
            nameId.setSPNameQualifier(samlAssertionData.getStrSpNameQualifier());
            nameId.setFormat(NameIDType.TRANSIENT);

            // Create the SubjectConfirmation

            SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
            SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) confirmationMethodBuilder.buildObject();
            confirmationMethod.setAddress("10.10.10.112");
            //confirmationMethod.setRecipient("https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/" + customerId);
            confirmationMethod.setRecipient("https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching");
            confirmationMethod.setRecipient("https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching");
            //https://dpdevcr03-coaching.healthmedia.com/mhmsite/startsso
            confirmationMethod.setNotOnOrAfter(now.plusMinutes(2));

            SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
            SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
            subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
            subjectConfirmation.setSubjectConfirmationData(confirmationMethod);

            // Create the Subject
            SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(Subject.DEFAULT_ELEMENT_NAME);
            Subject subject = (Subject) subjectBuilder.buildObject();
            subject.setNameID(nameId);
            subject.getSubjectConfirmations().add(subjectConfirmation);


            // Create Authentication Statement
            SAMLObjectBuilder subjectLocalityBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(SubjectLocality.DEFAULT_ELEMENT_NAME);
            SubjectLocality subjectLocality = (SubjectLocality)subjectLocalityBuilder.buildObject();
            subjectLocality.setAddress("10.10.10.112");

            SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
            AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();

            authnStatement.setSubjectLocality(subjectLocality);
            DateTime now2 = new DateTime();
            authnStatement.setAuthnInstant(now2);
            authnStatement.setSessionIndex(samlAssertionData.getSessionId());
//			authnStatement.setSessionNotOnOrAfter(now2.plus(assertionData.getMaxSessionTimeoutInMinutes()));

            SAMLObjectBuilder authContextBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
            AuthnContext authnContext = (AuthnContext) authContextBuilder.buildObject();

            SAMLObjectBuilder authContextClassRefBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
            AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) authContextClassRefBuilder.buildObject();
            authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

            authnContext.setAuthnContextClassRef(authnContextClassRef);
            authnStatement.setAuthnContext(authnContext);

            // Builder Attributes
            SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
            AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

            // Create the attribute statement
            Map<String, String> attributes = samlAssertionData.getAttributes();
            if (attributes != null) {
                Set<String> keySet = attributes.keySet();
                for (String key : keySet) {
                    Attribute attrFirstName = buildStringAttribute(key, attributes.get(key), getSAMLBuilder());
                    attrStatement.getAttributes().add(attrFirstName);
                }
            }

            // Create the do-not-cache condition
//			SAMLObjectBuilder doNotCacheConditionBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(OneTimeUse.DEFAULT_ELEMENT_NAME);
//			Condition condition = (Condition) doNotCacheConditionBuilder.buildObject();
            SAMLObjectBuilder audienceBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(Audience.DEFAULT_ELEMENT_NAME);
            Audience audience = (Audience)audienceBuilder.buildObject();
            audience.setAudienceURI("sp_localhost_coaching");

            SAMLObjectBuilder audienceRestrictionBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
            AudienceRestriction audienceRestriction= (AudienceRestriction)audienceRestrictionBuilder.buildObject();
            audienceRestriction.getAudiences().add(audience);



            SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
            Conditions conditions = (Conditions) conditionsBuilder.buildObject();
            conditions.setNotBefore(now);
            conditions.setNotOnOrAfter(now.plusMinutes(2));
            conditions.getAudienceRestrictions().add(audienceRestriction);

            // Create Issuer
            SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            Issuer issuer = (Issuer) issuerBuilder.buildObject();
            issuer.setFormat(NameIDType.ENTITY);
            issuer.setValue(samlAssertionData.getStrIssuer());

            // Create the assertion
            SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) this.getSAMLBuilder().getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
            Assertion assertion = (Assertion) assertionBuilder.buildObject();
            assertion.setID("_7d0a339c9179d3435c3b1a44946b2378");
            assertion.setIssuer(issuer);
            assertion.setIssueInstant(now);
            assertion.setVersion(SAMLVersion.VERSION_20);
            assertion.setSubject(subject);
            assertion.getAuthnStatements().add(authnStatement);
            assertion.getAttributeStatements().add(attrStatement);
            assertion.setConditions(conditions);

            return assertion;
        } catch (Exception e) {
            log.error("Exception", e);
            return null;
        }
    }

    public Attribute buildStringAttribute(String name, String value, XMLObjectBuilderFactory builderFactory) throws ConfigurationException {
        SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) getSAMLBuilder().getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        Attribute attrFirstName = (Attribute) attrBuilder.buildObject();
        attrFirstName.setName(name);
        attrFirstName.setNameFormat(Attribute.UNSPECIFIED);

        // Set custom Attributes
        XMLObjectBuilder stringBuilder = getSAMLBuilder().getBuilder(XSString.TYPE_NAME);
        XSString attrValueFirstName = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrValueFirstName.setValue(value);

        attrFirstName.getAttributeValues().add(attrValueFirstName);
        return attrFirstName;
    }



    private static XMLObjectBuilderFactory builderFactory;

    private XMLObjectBuilderFactory getSAMLBuilder() throws ConfigurationException {

        if (builderFactory == null) {
            DefaultBootstrap.bootstrap();
            builderFactory = Configuration.getBuilderFactory();
        }

        return builderFactory;
    }


    public void signAssertion(Assertion assertion) {
        Credential signingCredential = this.getSigningCredential();

        Signature signature = (Signature) Configuration.getBuilderFactory()
            .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
            .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(signingCredential);

        // This is also the default if a null SecurityConfiguration is specified
        SecurityConfiguration secConfig = Configuration.getGlobalSecurityConfiguration();
//        If null this would result in the default KeyInfoGenerator being used
//        String keyInfoGeneratorProfile = "XMLSignature";

        try {
            SecurityHelper.prepareSignatureParams(signature, signingCredential, secConfig, null);
        } catch (org.opensaml.xml.security.SecurityException e) {
            e.printStackTrace();
        }

        assertion.setSignature(signature);

        try {
            Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        } catch (MarshallingException e) {
            e.printStackTrace();
        }

        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    private Credential getSigningCredential() {
        KeyStore ks = null;
        FileInputStream fis = null;
        char[] password = this.password.toCharArray();

        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            log.error("Error while Intializing Keystore", e);
        }
        //BufferedInputStream bis = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(this.certificateFileName));
        //fis = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(this.certificateFileName)));
        try {
            fis = new FileInputStream("/Users/manojsingh/jnj/samlwebtest/src/main/resources/certs/" + this.certificateFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Load KeyStore
        try {
            ks.load(fis, password);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to Load the KeyStore:: ", e);
        } catch (CertificateException e) {
            log.error("Failed to Load the KeyStore:: ", e);
        } catch (IOException e) {
            log.error("Failed to Load the KeyStore:: ", e);
        }

        try {
            fis.close();
        } catch (IOException e) {
            log.error("Failed to close file stream:: ", e);
        }

        // Get Private Key Entry From Certificate
        KeyStore.PrivateKeyEntry pkEntry = null;

        try {

            Key privateKey = ks.getKey(this.certificateAliasName, this.password.toCharArray());
            pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(this.certificateAliasName, new KeyStore.PasswordProtection(this.password.toCharArray()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to Get Private Entry From the keystore:: " + this.certificateFileName, e);
        } catch (UnrecoverableEntryException e) {
            log.error("Failed to Get Private Entry From the keystore:: " + this.certificateFileName, e);
        } catch (KeyStoreException e) {
            log.error("Failed to Get Private Entry From the keystore:: " + this.certificateFileName, e);
        }
        PrivateKey pk = pkEntry.getPrivateKey();

        X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPrivateKey(pk);

        log.info("Private Key" + pk.toString());

        return credential;

    }


    //Create SAML Response
    public Response createSamlResponse(DateTime now, Assertion assertion) {
        SAMLObjectBuilder<Response> responseBuilder = (SAMLObjectBuilder<Response>) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
        Response response = responseBuilder.buildObject();
        response.setDestination("https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching");
        response.setID(this.generateAssertionId());
        response.setIssueInstant(now);
        response.setVersion(SAMLVersion.VERSION_20);
        response.setStatus(buildStatus(StatusCode.SUCCESS_URI));
        response.getAssertions().add(assertion);

        return response;
    }

    private Status buildStatus(String statusCodeValue){
        log.debug("Building StatusCode");
        StatusCode statusCode=((SAMLObjectBuilder<StatusCode>)builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME)).buildObject();
        statusCode.setValue(statusCodeValue);
        log.debug("Set StatusCode to: " + statusCodeValue);
        log.debug("Building Status");
        Status status=((SAMLObjectBuilder<Status>)builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME)).buildObject();
        status.setStatusCode(statusCode);
        return status;
    }

    public String generateAssertionId(){
        Generex generex = new Generex(ASSERTION_ID_REGEX);
        String assertionId = generex.random();
        return assertionId;
    }

}
