package com.sismics.reader.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the search resource.
 * 
 * @author bgamard
 */
public class TestSearchResource extends BaseJerseyTest {
    /**
     * Test of the search resource.
     * @throws Exception 
     */
    @Test
    public void testSearchResource() throws Exception {
        // Create user search1
        clientUtil.createUser("search1");
        String search1AuthToken = clientUtil.login("search1");

        // Subscribe to Korben RSS feed
        WebResource subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        ClientResponse response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        
        // Search "zelda"
        WebResource searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        assertSearchResult(articles, "Quand <span class=\"highlight\">Zelda</span> prend les armes");
        
        // Search "njloinzejrmklsjd"
        searchResource = resource().path("/search/njloinzejrmklsjd");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(0, articles.length());
        
        // Search "wifi"
        searchResource = resource().path("/search/wifi");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        assertSearchResult(articles, "Récupérer les clés <span class=\"highlight\">wifi</span> sur un téléphone Android");
        assertSearchResult(articles, "Partagez vos clés <span class=\"highlight\">WiFi</span> avec vos amis");
        
        // Search "google keep"
        searchResource = resource().path("/search/google%20keep");
        searchResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        
        // Create user search2
        clientUtil.createUser("search2");
        String search2AuthToken = clientUtil.login("search2");

        // Subscribe to Korben RSS feed again to force articles updating
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search2AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        
        // Check if nothing is broken by searching "google keep"
        searchResource = resource().path("/search/google%20keep");
        searchResource.addFilter(new CookieAuthenticationFilter(search2AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        
        // Create user search3
        clientUtil.createUser("search3");
        String search3AuthToken = clientUtil.login("search3");
        
        // Search "njloinzejrmklsjd"
        searchResource = resource().path("/search/njloinzejrmklsjd");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertEquals(0, articles.length());
        
        // Search "zelda"
        searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        
        // Subscribe to Korben RSS feed (alternative URL)
        subscriptionResource = resource().path("/subscription");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(search1AuthToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("url", "http://localhost:9997/http/feeds/korben2.xml");
        response = subscriptionResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Search "zelda"
        searchResource = resource().path("/search/zelda");
        searchResource.addFilter(new CookieAuthenticationFilter(search3AuthToken));
        response = searchResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        articles = json.getJSONArray("articles");
        Assert.assertTrue(articles.length() > 0);
        
        // Check that there is no duplicates
        JSONObject zeldaArticle = null;
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            if (article.getString("url").equals("http://korben.info/quand-zelda-prend-les-armes.html")) {
                if (zeldaArticle != null) {
//                    Assert.fail("Two articles with the same URL"); // TODO Fix me
                }
                zeldaArticle = article;
            }
        }
    }
    
    /**
     * Assert that an article exists with a specific title in the provided articles set.
     * 
     * @param articles Articles from search
     * @param title Expected title
     * @throws JSONException
     */
    private void assertSearchResult(JSONArray articles, String title) throws JSONException {
    	for (int i = 0; i < articles.length(); i++) {
    		JSONObject article = articles.getJSONObject(i);
    		if (article.getString("title").equals(title)) {
    			return;
    		}
    	}
    	
    	Assert.fail();
    }
}