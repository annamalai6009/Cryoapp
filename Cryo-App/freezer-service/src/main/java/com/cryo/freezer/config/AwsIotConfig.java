//package com.cryo.freezer.config;
//
//import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.openssl.PEMKeyPair;
//import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.integration.channel.DirectChannel;
//import org.springframework.integration.core.MessageProducer;
//import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
//import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
//import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
//import org.springframework.messaging.MessageChannel;
//
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManagerFactory;
//import java.io.InputStreamReader;
//import java.security.KeyStore;
//import java.security.Security;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.security.KeyPair;
//
//@Configuration
//public class AwsIotConfig {
//
//    @Value("${aws.iot.endpoint:ssl://YOUR-ENDPOINT-HERE:8883}")
//    private String awsIotEndpoint;
//
//    @Bean
//    public MqttPahoClientFactory mqttClientFactory() throws Exception {
//        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//        MqttConnectOptions options = new MqttConnectOptions();
//
//        options.setServerURIs(new String[]{awsIotEndpoint});
//        options.setCleanSession(true);
//        options.setAutomaticReconnect(true);
//
//        // 🔒 LOAD THE CERTIFICATES SAFELY
//        options.setSocketFactory(getSocketFactory());
//
//        factory.setConnectionOptions(options);
//        return factory;
//    }
//
//    @Bean
//    public MessageChannel mqttInputChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    public MessageProducer inbound() throws Exception {
//        String clientId = "CryoServer_" + System.currentTimeMillis();
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory(),
//                        "#" // Listen to EVERYTHING
//                );
//        adapter.setCompletionTimeout(5000);
//        adapter.setConverter(new DefaultPahoMessageConverter());
//        adapter.setQos(1);
//        adapter.setOutputChannel(mqttInputChannel());
//        return adapter;
//    }
//
//    // 🔐 ROBUST HELPER: Reads ANY key format using Bouncy Castle
//    private SSLSocketFactory getSocketFactory() throws Exception {
//        Security.addProvider(new BouncyCastleProvider());
//
//        // 1. Load CA Certificate
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        X509Certificate caCert = (X509Certificate) cf.generateCertificate(new ClassPathResource("root-ca.pem").getInputStream());
//
//        // 2. Load Client Certificate
//        X509Certificate clientCert = (X509Certificate) cf.generateCertificate(new ClassPathResource("certificate.pem").getInputStream());
//
//        // 3. Load Private Key (The Robust Way)
//        Object keyObject;
//        try (PEMParser pemParser = new PEMParser(new InputStreamReader(new ClassPathResource("private.key").getInputStream()))) {
//            keyObject = pemParser.readObject();
//        }
//
//        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
//        KeyPair keyPair;
//
//        if (keyObject instanceof PEMKeyPair) {
//            // PKCS#1 format (BEGIN RSA PRIVATE KEY)
//            keyPair = converter.getKeyPair((PEMKeyPair) keyObject);
//        } else if (keyObject instanceof PrivateKeyInfo) {
//            // PKCS#8 format (BEGIN PRIVATE KEY)
//            keyPair = new KeyPair(null, converter.getPrivateKey((PrivateKeyInfo) keyObject));
//        } else {
//            throw new IllegalArgumentException("Unknown key format: " + keyObject.getClass());
//        }
//
//        // 4. Create KeyStore
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("ca-cert", caCert);
//        keyStore.setCertificateEntry("cert", clientCert);
//        keyStore.setKeyEntry("key", keyPair.getPrivate(), "password".toCharArray(), new java.security.cert.Certificate[]{clientCert});
//
//        // 5. Create SSL Context
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        kmf.init(keyStore, "password".toCharArray());
//
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        tmf.init(keyStore);
//
//        SSLContext context = SSLContext.getInstance("TLSv1.2");
//        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//        return context.getSocketFactory();
//    }
//}