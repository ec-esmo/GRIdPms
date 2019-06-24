/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.utils;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.sp.SPUtil;
import eu.eidas.sp.SpAuthenticationRequestData;
import eu.eidas.sp.SpProtocolEngineFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author nikos
 */
public class EidasSpToolExtra {

    public static SpAuthenticationRequestData generateEIDASRequest(final ArrayList<String> pal, final String citizenCountry,
            final String serviceProviderCountry, int qaaLvl) {
        System.out.println(Paths.get(".", new String[0]).toAbsolutePath().normalize().toString());
        final Properties configs = SPUtil.loadSPConfigs();
        final String spId = configs.getProperty("provider.name");
        final String providerName = configs.getProperty("provider.name");
        final String nodeUrl = configs.getProperty("country1.url");
        final ProtocolEngineI protocolEngine = (ProtocolEngineI) SpProtocolEngineFactory.getSpProtocolEngine("SP");
        final EidasAuthenticationRequest.Builder reqBuilder = new EidasAuthenticationRequest.Builder();
        final ImmutableSortedSet<AttributeDefinition<?>> allSupportedAttributesSet = (ImmutableSortedSet<AttributeDefinition<?>>) protocolEngine.getProtocolProcessor().getAllSupportedAttributes();
        final List<AttributeDefinition<?>> reqAttrList = new ArrayList<AttributeDefinition<?>>((Collection<? extends AttributeDefinition<?>>) allSupportedAttributesSet);
        for (final AttributeDefinition<?> attributeDefinition : allSupportedAttributesSet) {
            final String attributeName = attributeDefinition.getNameUri().toASCIIString();
            System.out.println("Checking " + attributeName);
            if (!pal.contains(attributeName)) {
                reqAttrList.remove(attributeDefinition);
            }
        }
        final ImmutableAttributeMap reqAttrMap = new ImmutableAttributeMap.Builder().putAll((Collection) reqAttrList).build();
        reqBuilder.requestedAttributes(reqAttrMap);
        reqBuilder.destination(nodeUrl);
        reqBuilder.providerName(providerName);
//        final int qaaLvl = Integer.parseInt(configs.getProperty("sp.qaalevel"));
        reqBuilder.levelOfAssurance(LevelOfAssurance.LOW.stringValue());
        if (qaaLvl == 3) {
            reqBuilder.levelOfAssurance(LevelOfAssurance.SUBSTANTIAL.stringValue());
        } else if (qaaLvl == 4) {
            reqBuilder.levelOfAssurance(LevelOfAssurance.HIGH.stringValue());
        }
        reqBuilder.spType("public");
        reqBuilder.levelOfAssuranceComparison(LevelOfAssuranceComparison.fromString("minimum").stringValue());
        reqBuilder.nameIdFormat("urn:oasis:names:tc:saml:1.1:nameid-format:unspecified");
        reqBuilder.binding(EidasSamlBinding.EMPTY.getName());
        reqBuilder.issuer(configs.getProperty("sp.metadata.url"));
        reqBuilder.serviceProviderCountryCode(serviceProviderCountry);
        reqBuilder.citizenCountryCode(citizenCountry);
        IRequestMessage binaryRequestMessage = null;
        String ncName = null;
        try {
            ncName = SAMLEngineUtils.generateNCName();
            reqBuilder.id(ncName);
            final EidasAuthenticationRequest authnRequest = (EidasAuthenticationRequest) reqBuilder.build();
            binaryRequestMessage = protocolEngine.generateRequestMessage((IAuthenticationRequest) authnRequest, configs.getProperty("country1.metadata.url"));
            System.out.println(">>>>>>>>>>>> " + authnRequest.getId() + "-" + authnRequest.getIssuer());
        } catch (EIDASSAMLEngineException e) {
            final String errorMessage = "Could not generate token for Saml Request: " + e.getMessage();
            System.out.println(errorMessage);
        } catch (Exception e2) {
            final String errorMessage = "Could not generate token for Saml Request: " + e2.getMessage();
            System.out.println(errorMessage);
            e2.printStackTrace();
        }
        final byte[] token = binaryRequestMessage.getMessageBytes();
        final SpAuthenticationRequestData data = new SpAuthenticationRequestData(EidasStringUtil.encodeToBase64(token), binaryRequestMessage.getRequest().getId());
        return data;
    }

}
