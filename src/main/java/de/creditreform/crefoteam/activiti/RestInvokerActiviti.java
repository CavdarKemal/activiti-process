package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerApache4;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class RestInvokerActiviti extends RestInvokerApache4 {
    public RestInvokerActiviti(String restURL, String userName, String password) {
        super(restURL, userName, password);
    }

    @Override
    public RestInvoker init(Integer timeOutMillis) {
        closeClient();

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(getUserName(), getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultCredentialsProvider(provider);
        if ((timeOutMillis != null) && (timeOutMillis >= 0)) {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeOutMillis).setSocketTimeout(timeOutMillis).build();
            clientBuilder.setDefaultRequestConfig(requestConfig);
        }
        setHttpClient(clientBuilder.build());
        return this;
    }

    @Override
    public String toString() {
        return String.format("URL: %s:%s@%s", getUserName(), getPassword(), getRestURI());
    }
}
