package io.ucoin.app.config;

/*
 * #%L
 * Tutti :: Persistence
 * $Id: TuttiConfigurationOption.java 1441 2013-12-09 20:13:47Z tchemit $
 * $HeadURL: http://svn.forge.codelutin.com/svn/tutti/trunk/tutti-persistence/src/main/java/fr/ifremer/tutti/TuttiConfigurationOption.java $
 * %%
 * Copyright (C) 2012 - 2013 Ifremer
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import java.net.URL;

/**
 * All application configuration options.
 * 
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public enum ConfigurationOption {

    // ------------------------------------------------------------------------//
    // -- READ-ONLY OPTIONS ---------------------------------------------------//
    // ------------------------------------------------------------------------//

    VERSION(
            "ucoin.version",
            "ucoin.config.option.version.description",
            "1.0",
            String.class),

    SITE_URL(
            "ucoin.site.url",
            "ucoin.config.option.site.url.description",
            "http://ucoin.io/ucoinj",
            URL.class),

    ORGANIZATION_NAME(
            "ucoin.organizationName",
            "ucoin.config.option.organizationName.description",
            "e-is.pro",
            String.class),

    INCEPTION_YEAR(
            "ucoin.inceptionYear",
            "ucoin.config.option.inceptionYear.description",
            "2011",
            Integer.class),

    // ------------------------------------------------------------------------//
    // -- DATA CONSTANTS --------------------------------------------------//
    // ------------------------------------------------------------------------//

    // ------------------------------------------------------------------------//
    // -- READ-WRITE OPTIONS --------------------------------------------------//
    // ------------------------------------------------------------------------//

    NODE_CURRENCY(
            "ucoin.node.currency",
            "ucoin.config.option.node.currency.description",
            "meta_brouzouf",
            String.class,
            false),

    NODE_PROTOCOL(
            "ucoin.node.protocol",
            "ucoin.config.option.node.protocol.description",
            "http",
            String.class,
            false),

    NODE_HOST(
            "ucoin.node.host",
            "ucoin.config.option.node.host.description",
            "metab.ucoin.io",
            //"server.e-is.pro",
            String.class,
            false),

    NODE_PORT(
            "ucoin.node.port",
            "ucoin.config.option.node.port.description",
            //"9101",
            "9201",
            Integer.class,
            false),

    NODE_TIMEOUT(
            "ucoin.node.timeout",
            "ucoin.config.option.node.timeout.description",
            "3000",
            Integer.class,
            false),

    FORUM_URL(
            "ucoin.forum.url",
                    "ucoin.config.option.forum.url.description",
                    "http://forum.ucoin.io",
            String.class,
            false)

    // ------------------------------------------------------------------------//
    // -- EXT CONSTANTS --------------------------------------------------//
    // ------------------------------------------------------------------------//

    ;

    /** Configuration key. */
    private final String key;

    /** I18n key of option description */
    private final String description;

    /** Type of option */
    private final Class<?> type;

    /** Default value of option. */
    private String defaultValue;

    /** Flag to not keep option value on disk */
    private boolean isTransient;

    /** Flag to not allow option value modification */
    private boolean isFinal;

    ConfigurationOption(String key,
            String description,
            String defaultValue,
            Class<?> type,
            boolean isTransient) {
        this.key = key;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.isTransient = isTransient;
        this.isFinal = isTransient;
    }

    ConfigurationOption(String key,
            String description,
            String defaultValue,
            Class<?> type) {
        this(key, description, defaultValue, type, true);
    }

    
    public String getKey() {
        return key;
    }

    
    public Class<?> getType() {
        return type;
    }

    
    public String getDescription() {
        return description;
    }

    
    public String getDefaultValue() {
        return defaultValue;
    }

    
    public boolean isTransient() {
        return isTransient;
    }

    
    public boolean isFinal() {
        return isFinal;
    }

    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    
    public void setTransient(boolean newValue) {
        // not used
    }

    
    public void setFinal(boolean newValue) {
        // not used
    }
}
