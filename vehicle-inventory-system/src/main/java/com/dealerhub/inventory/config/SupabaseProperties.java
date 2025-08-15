package com.dealerhub.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    /** Project URL, e.g. https://xyzcompany.supabase.co */
    private String url;

    /** Service role key — server-side only, never exposed to the frontend. */
    private String serviceRoleKey;

    /** Storage bucket that holds vehicle photos. */
    private String storageBucket = "vehicle-photos";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServiceRoleKey() {
        return serviceRoleKey;
    }

    public void setServiceRoleKey(String serviceRoleKey) {
        this.serviceRoleKey = serviceRoleKey;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }
}
