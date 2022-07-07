<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : DIM2DataCite.xsl
    Created on : January 23, 2013, 1:26 PM
    Author     : pbecker, ffuerste
    Description: Converts metadata from DSpace Intermediat Format (DIM) into
                 metadata following the DataCite Schema for the Publication and
                 Citation of Research Data, Version 4.2
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dspace="http://www.dspace.org/xmlns/dspace/dim"
                xmlns="http://datacite.org/schema/kernel-4"
                version="2.0">
    
    <!-- CONFIGURATION -->
    <!-- We need the prefix to determine DOIs that were minted by ourself. -->
    <xsl:variable name="prefix">10.15480/882.</xsl:variable>
    <!-- The content of the following variable will be used as element publisher. -->
    <xsl:variable name="publisher">TUHH Universitätsbibliothek</xsl:variable>
    <!-- The content of the following variable will be used as element contributor with contributorType datamanager. -->
    <xsl:variable name="datamanager"><xsl:value-of select="$publisher" /></xsl:variable>
    <!-- The content of the following variable will be used as element contributor with contributorType hostingInstitution. -->
    <xsl:variable name="hostinginstitution"><xsl:value-of select="$publisher" /></xsl:variable>
    <!-- Please take a look into the DataCite schema documentation if you want to know how to use these elements.
         http://schema.datacite.org -->
    
    
    <!-- DO NOT CHANGE ANYTHING BELOW THIS LINE EXCEPT YOU REALLY KNOW WHAT YOU ARE DOING! -->
    
    <xsl:output method="xml" indent="yes" encoding="utf-8" />
    
    <!-- Don't copy everything by default! -->
    <xsl:template match="@* | text()" />

    <xsl:variable name="placeholder">#PLACEHOLDER_PARENT_METADATA_VALUE#</xsl:variable>

    <xsl:template match="/dspace:dim[@dspaceType='ITEM']">
        <!--
            org.dspace.identifier.doi.DataCiteConnector uses this XSLT to
            transform metadata for the DataCite metadata store. This crosswalk
            should only be used, when it is ensured that all mandatory
            properties are in the metadata of the item to export.
            The classe named above respects this.
        -->
        <resource xmlns="http://datacite.org/schema/kernel-4"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.4/metadata.xsd">

            <!-- 
                MANDATORY PROPERTIES
            -->

            <!-- 
                DataCite (1)
                Template Call for DOI identifier.
                Occ: 1
            -->
            <!--
                dc.identifier.uri may contain more than one DOI, e.g. if the
                repository contains an item that is published by a publishing.
                company as well. We have to ensure to use URIs of our prefix
                as primary identifiers only.
            -->
            <xsl:apply-templates select="//dspace:field[@mdschema='tuhh' and @element='identifier' and @qualifier='doi']" />

            <!-- 
                DataCite (2)
                Add creator information. 
            -->
            <creators>
                <xsl:call-template name="creator" />
            </creators>

            <!-- 
                DataCite (3)
                Add Title information. 
            -->
            <titles>
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='dc' and @element='title']">
                        <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='title']" />
                    </xsl:when>
                    <xsl:otherwise>
                        <title>(:unas) unassigned</title>
                    </xsl:otherwise>
                </xsl:choose>
            </titles>
            
            <!-- 
                DataCite (4)
                Add Publisher information from configuration above
                Occ: 1
                Use dc.publisher if it exists, use $publisher otherwise.
            -->
            <xsl:element name="publisher">
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='dc' and @element='publisher']">
                        <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='publisher'][1]" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$publisher" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>

            <!-- 
                DataCite (5)
                Add PublicationYear information
            -->
            <publicationYear>
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='dc' and @element='date' and @qualifier='issued']">
                        <xsl:value-of select="substring(//dspace:field[@mdschema='dc' and @element='date' and @qualifier='issued'], 1, 4)" />
                    </xsl:when>
                    <xsl:when test="//dspace:field[@mdschema='dc' and @element='date' and @qualifier='available']">
                        <xsl:value-of select="substring(//dspace:field[@mdschema='dc' and @element='date' and @qualifier='available'], 1, 4)" />
                    </xsl:when>
                    <xsl:when test="//dspace:field[@mdschema='dc' and @element='date']">
                        <xsl:value-of select="substring(//dspace:field[@mdschema='dc' and @element='date'], 1, 4)" />
                    </xsl:when>
                    <xsl:otherwise>0000</xsl:otherwise>
                </xsl:choose>
            </publicationYear>

            <!-- 
                OPTIONAL PROPERTIES
            -->

            <!-- 
                DataCite (6)
                Template Call for subjects.
            -->  
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='subject']">
                <subjects>
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='subject']" />
                </subjects>
            </xsl:if>

            <!-- 
                DataCite (7)
                Add contributorType from configuration above.
                Template Call for Contributors
            --> 
            <contributors>
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">DataManager</xsl:attribute>
                    <xsl:element name="contributorName">
                        <xsl:value-of select="$datamanager"/>
                    </xsl:element>
                </xsl:element>
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">HostingInstitution</xsl:attribute>
                    <contributorName>
                        <xsl:value-of select="$hostinginstitution" />
                    </contributorName>
                </xsl:element>
                <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='contributor'][not(@qualifier='author')]" />
                <xsl:apply-templates select="//dspace:field[@mdschema='tuhh' and @element='contributor' and @qualifier='referee']" />
                <xsl:apply-templates select="//dspace:field[@mdschema='local' and @element='contributorPerson']" />
                <xsl:apply-templates select="//dspace:field[@mdschema='local' and @element='contributorCorporate']" />
                <xsl:apply-templates select="//dspace:field[@mdschema='datacite' and @element='contributor']" />
                <xsl:apply-templates select="//dspace:field[@mdschema='datacite' and @element='contributorCorporate']" />
            </contributors>

            <!-- 
                DataCite (8)
                Template Call for Dates
                Occ: 0-n
                Required Attribute: dataType - controlled list
            --> 
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='date' and
                        (@qualifier='accessioned'
                         or @qualifier='available'
                         or @qualifier='copyright'
                         or @qualifier='created'
                         or @qualifier='issued'
                         or @qualifier='submitted'
                         or @qualifier='updated')]" >
                <xsl:element name="dates">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='date' and
                        (@qualifier='accessioned'
                         or @qualifier='available'
                         or @qualifier='copyright'
                         or @qualifier='created'
                         or @qualifier='issued'
                         or @qualifier='submitted'
                         or @qualifier='updated')]" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (9)
                Templacte Call for Language
                Occ: 0-1
                Format: IETF BCP 47 or ISO 639-1
            -->
            <xsl:apply-templates select="(//dspace:field[@mdschema='dc' and @element='language' and (@qualifier='iso' or @qualifier='rfc3066')])[1]" />

            <!--
                DataCite (10)
                Template call for ResourceType
                DataCite allows the ResourceType to ouccre not more than once.
            -->
            <xsl:apply-templates select="(//dspace:field[@mdschema='datacite' and @element='resourceTypeGeneral'])" />

            <!--
                DataCite (11)
                Add alternativeIdentifiers.
                This element is important as it is used to recognize for which
                DSpace object a DOI is reserved for.
                See the primary identifier for which the doi is registered.
                Occ: 0-n
                Required Attribute: alternateIdentifierType (free format)
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='identifier' and not(@qualifier='issn') and not(@qualifier='scopus') and not(starts-with(., concat('http://dx.doi.org/', $prefix)))]">
                <xsl:element name="alternateIdentifiers">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='identifier' and not(@qualifier='issn') and not(@qualifier='scopus') and not(starts-with(., concat('http://dx.doi.org/', $prefix)))]" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (12)
                Add related identifiers
            -->
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='relation' and @qualifier='issupplementedby'] or //dspace:field[@mdschema='datacite' and @element='relation'] or //dspace:field[@mdschema='tuhh' and @element='publisher']">
                <xsl:element name="relatedIdentifiers">
                    <xsl:apply-templates select="//dspace:field[@mdschema='tuhh' and @element='relation' and @qualifier='issupplementedby']" />
                    <xsl:apply-templates select="//dspace:field[@mdschema='datacite' and @element='relation']" />
                    <xsl:apply-templates select="//dspace:field[@mdschema='tuhh' and @element='publisher']" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (12)
                Add sizes.
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='format' and @qualifier='extent']">
                <xsl:element name="sizes">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='format' and @qualifier='extent']" />
                </xsl:element>
            </xsl:if>

            <!-- DataCite (13)
                Add formats.
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='format'][not(@qualifier='extent')]">
                <xsl:element name="formats">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='format'][not(@qualifier='extent')]" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (16)
                Rights.
                Occ: 0-1
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='rights'] or //dspace:field[@mdschema='item' and @element='grantfulltext']">
                <xsl:element name="rightsList">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='rights'][not(@qualifier='creditline')]" />
                    <xsl:apply-templates select="//dspace:field[@mdschema='item' and @element='grantfulltext']" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (17)
                Add descriptions.
                Occ: 0-n
                Required Attribute: descriptionType - controlled list
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='description' and (@qualifier='abstract' or @qualifier='tableofcontents' or not(@qualifier))]">
                <xsl:element name="descriptions">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='description' and (@qualifier='abstract' or @qualifier='tableofcontents' or not(@qualifier))]" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (18)
                GeoLocation
                DSpace currently doesn't store geolocations.
            -->

            <!--
                DataCite (19)
                Funding References
                Occ: 0-n
            -->
            <xsl:if test="//dspace:field[(@mdschema='crisitem' and @element='project' and @qualifier='funder') or (@mdschema='dc' and @element='description' and @qualifier='sponsorship')]">
                <xsl:element name="fundingReferences">
                    <xsl:call-template name="fundingReference" />
                </xsl:element>
            </xsl:if>

            <!--
                DataCite (20)
                Add related items
            -->
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='relation' and @qualifier='ispartof']">
                <xsl:element name="relatedItems">
                    <xsl:apply-templates select="//dspace:field[@mdschema='dc' and @element='relation' and @qualifier='ispartof']" />
                </xsl:element>
            </xsl:if>
        </resource>
    </xsl:template>
    

    <!--
        dc.identifier.uri may contain more than one DOI, e.g. if the
        repository contains an item that is published by a publishing.
        company as well. We have to ensure to use URIs of our prefix
        as primary identifiers only.
    -->
    <xsl:template match="//dspace:field[@mdschema='tuhh' and @element='identifier' and @qualifier='doi']">
        <identifier identifierType="DOI">
            <xsl:value-of select="."/>
        </identifier>
    </xsl:template>

    <!-- DataCite (2) :: Creator -->
    <xsl:template name="creator">
        <xsl:choose>
            <xsl:when test="//dspace:field[@mdschema='item' and @element='creatorOrcid'] or
                            //dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='editor'] or
                            //dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='other'] or
                            //dspace:field[@mdschema='local' and @element='contributorPerson' and @qualifier='editor'] or
                            //dspace:field[@mdschema='local' and @element='contributorCorporate' and @qualifier='editor']
            ">
              <xsl:choose>
              <xsl:when test="//dspace:field[@mdschema='item' and @element='creatorOrcid']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='creatorOrcid']">
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <creator>
                        <creatorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </creatorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='creatorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='creatorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </creator>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='editor']">
                <xsl:for-each select="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='editor']">
        <xsl:variable name="actualPersonName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
                <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:if test='.=$actualPersonName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <creator>
                    <xsl:choose>
                        <xsl:when test="contains(.,',')">
                        <creatorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." /><xsl:text> (Ed.)</xsl:text>
                        </creatorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        </xsl:when>
                        <xsl:otherwise>
                        <creatorName>
                            <xsl:attribute name="nameType">Organizational</xsl:attribute>
                            <xsl:value-of select="." /><xsl:text> (Ed.)</xsl:text>
                        </creatorName>
                        </xsl:otherwise>
                    </xsl:choose>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </creator>
                    </xsl:if>
                </xsl:for-each>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='other']">
                <xsl:for-each select="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='other']">
        <xsl:variable name="actualPersonName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
                <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:if test='.=$actualPersonName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <creator>
                    <xsl:choose>
                        <xsl:when test="contains(.,',')">
                        <creatorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </creatorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        </xsl:when>
                        <xsl:otherwise>
                        <creatorName>
                            <xsl:attribute name="nameType">Organizational</xsl:attribute>
                            <xsl:value-of select="." />
                        </creatorName>
                        </xsl:otherwise>
                    </xsl:choose>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </creator>
                    </xsl:if>
                </xsl:for-each>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='local' and @element='contributorPerson' and @qualifier='editor']">
                <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <creator>
                        <creatorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." /><xsl:text> (Ed.)</xsl:text>
                        </creatorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </creator>
                </xsl:for-each>
                </xsl:if>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='local' and @element='contributorCorporate' and @qualifier='editor']">
            <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
            <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorCorpGND']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorCorpGND']">
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <creator>
                        <creatorName>
                            <xsl:attribute name="nameType">Organizational</xsl:attribute>
                            <xsl:value-of select="." /><xsl:text> (Ed.)</xsl:text>
                        </creatorName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorCorpROR'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://ror.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ROR</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorCorpROR'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </creator>
                </xsl:for-each>
            </xsl:if>
            </xsl:if>
            </xsl:otherwise>
            </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <creator>
                    <creatorName>
                        <xsl:attribute name="nameType">Personal</xsl:attribute>
                        <xsl:text>(:unkn) unknown</xsl:text>
                    </creatorName>
                </creator>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- DataCite (2) :: Creator -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='author']">
        <creator>
            <creatorName>
                <xsl:attribute name="nameType">Personal</xsl:attribute>
                <xsl:value-of select="." />
            </creatorName>
            <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
            <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
        </creator>
    </xsl:template>

    <!-- DataCite (2) :: Creator -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='editor']">
        <creator>
            <creatorName>
                <xsl:attribute name="nameType">Personal</xsl:attribute>
                <xsl:value-of select="." />
            </creatorName>
            <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
            <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
        </creator>
    </xsl:template>

    <!-- DataCite (2) :: Creator -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='contributor' and @qualifier='other']">
        <creator>
            <creatorName>
                <xsl:attribute name="nameType">Personal</xsl:attribute>
                <xsl:value-of select="." />
            </creatorName>
            <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
            <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
        </creator>
    </xsl:template>

    <!-- DataCite (3) :: Title -->
    <xsl:template match="dspace:field[@mdschema='dc' and @element='title']">
        <xsl:element name="title">
            <xsl:if test="@qualifier='alternative'">
                <xsl:attribute name="titleType">AlternativeTitle</xsl:attribute>
            </xsl:if>
            <!-- DSpace does include niehter a dc.title.subtitle nor a.
                 dc.title.translated. If necessary, please create those in the.
                 metadata field registry. -->
            <xsl:if test="@qualifier='subtitle'">
                <xsl:attribute name="titleType">Subtitle</xsl:attribute>
            </xsl:if>
            <xsl:if test="@qualifier='translated'">
                <xsl:attribute name="titleType">TranslatedTitle</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (6), DataCite (6.1)
        Adds subject and subjectScheme information
    
        "This term is intended to be used with non-literal values as defined in the 
        DCMI Abstract Model (http://dublincore.org/documents/abstract-model/). 
        As of December 2007, the DCMI Usage Board is seeking a way to express 
        this intention with a formal range declaration." 
        (http://dublincore.org/documents/dcmi-terms/#terms-subject)
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='subject']">
        <xsl:if test="not(contains(@qualifier, 'code'))">
        <xsl:element name="subject">
            <xsl:if test="@qualifier">
                <xsl:attribute name="subjectScheme"><xsl:value-of select="upper-case(@qualifier)" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(., ':')">
                <xsl:attribute name="classificationCode"><xsl:value-of select="substring-before(., ':')" /></xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="contains(., ':')">
                    <xsl:value-of select="substring-after(., ':')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="." />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        </xsl:if>
        <!-- Do not show DDC code as we cannot resolve it smoothly to a readable form
        <xsl:if test="@qualifier='ddccode'">
        <xsl:element name="subject">
            <xsl:if test="@qualifier">
                <xsl:attribute name="subjectScheme">DDC</xsl:attribute>
            </xsl:if>
            <xsl:attribute name="classificationCode"><xsl:value-of select="." /></xsl:attribute>
            <xsl:value-of select="." />
        </xsl:element>
        </xsl:if>
        -->
    </xsl:template>

    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='contributor'][not(@qualifier='author')]">
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
        <!-- if this is an editor and we do not have an item.creatorOrcid, we should display the editor(s) here) -->
        <xsl:if test="(//dspace:field[@mdschema='item' and @element='creatorOrcid']!='' and $qualifier='editor') or $qualifier!='editor'">
        <xsl:choose>
            <xsl:when test="@qualifier='editor'">
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">Editor</xsl:attribute>
                    <contributorName>
                        <xsl:value-of select="." />
                    </contributorName>
                </xsl:element>
            </xsl:when>
            <xsl:when test="@qualifier='advisor'">
            <xsl:if test="//dspace:field[@mdschema='item' and @element='advisorOrcid']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='advisorOrcid']">
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType">RelatedPerson</xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='advisorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='advisorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:for-each>
            </xsl:if>
            </xsl:when>
            <xsl:when test="@qualifier='illustrator'">
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">Other</xsl:attribute>
                    <contributorName>
                        <xsl:value-of select="." />
                    </contributorName>
                </xsl:element>
            </xsl:when>
            <xsl:when test="@qualifier='other'">
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">Other</xsl:attribute>
                    <contributorName>
                        <xsl:value-of select="." />
                    </contributorName>
                </xsl:element>
            </xsl:when>
            <xsl:when test="not(@qualifier)">
                <xsl:element name="contributor">
                    <xsl:attribute name="contributorType">Other</xsl:attribute>
                    <contributorName>
                        <xsl:value-of select="." />
                    </contributorName>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
        </xsl:if>
    </xsl:template>
    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='tuhh' and @element='contributor' and @qualifier='referee']">
            <xsl:if test="//dspace:field[@mdschema='item' and @element='refereeOrcid']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='refereeOrcid']">
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType">Other</xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='refereeGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='refereeGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:for-each>
            </xsl:if>
    </xsl:template>
    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='local' and @element='contributorPerson']">
	<xsl:variable name="actualPersonName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
            <!-- if this is an editor and we do not have an item.creatorOrcid, we should display the editor(s) here) -->
            <xsl:if test="(//dspace:field[@mdschema='item' and @element='creatorOrcid']!='' and $qualifier='editor') or $qualifier!='editor'">
            <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorOrcid']">
		<xsl:if test='.=$actualPersonName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType">
        <xsl:choose>
            <xsl:when test="$qualifier='editor'">
                    <xsl:text>Editor</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                    <xsl:text>Other</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
                        </xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
            </xsl:if>
    </xsl:template>
    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='local' and @element='contributorCorporate']">
	<xsl:variable name="actualCorporateName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
            <xsl:if test="(//dspace:field[@mdschema='item' and @element='creatorOrcid']!='' and $qualifier='editor') or $qualifier!='editor'">
            <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorCorpGND']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='contributorCorpGND']">
		<xsl:if test='.=$actualCorporateName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType">
        <xsl:choose>
            <xsl:when test="$qualifier='editor'">
                    <xsl:text>Editor</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                    <xsl:text>Other</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
                        </xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Organizational</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='contributorCorpROR'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://ror.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ROR</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='contributorCorpROR'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
            </xsl:if>
    </xsl:template>
    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='datacite' and @element='contributor']">
	<xsl:variable name="actualPersonName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
            <xsl:if test="//dspace:field[@mdschema='item' and @element='dataciteContributorOrcid']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='dataciteContributorOrcid']">
		<xsl:if test='.=$actualPersonName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType"><xsl:value-of select="$qualifier" /></xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Personal</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <givenName><xsl:value-of select="substring-after(., ',')"/></givenName>
                        <familyName><xsl:value-of select="substring-before(., ',')"/></familyName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://orcid.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ORCID</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='dataciteContributorGND'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='dataciteContributorGND'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
    </xsl:template>
    <!-- 
        DataCite (7), DataCite (7.1)
        Adds contributor and contributorType information
    -->
    <xsl:template match="//dspace:field[@mdschema='datacite' and @element='contributorCorporate']">
	<xsl:variable name="actualCorporateName"><xsl:value-of select="." /></xsl:variable>
        <xsl:variable name="qualifier"><xsl:value-of select="@qualifier" /></xsl:variable>
            <xsl:if test="//dspace:field[@mdschema='item' and @element='dataciteContributorCorpGND']">
                <xsl:for-each select="//dspace:field[@mdschema='item' and @element='dataciteContributorCorpGND']">
		<xsl:if test='.=$actualCorporateName'>
                    <xsl:variable name="i">
                        <xsl:number value="position()" />
                    </xsl:variable>
                    <contributor>
                        <xsl:attribute name="contributorType">
                            <xsl:value-of select="$qualifier" />
                        </xsl:attribute>
                        <contributorName>
                            <xsl:attribute name="nameType">Organizational</xsl:attribute>
                            <xsl:value-of select="." />
                        </contributorName>
                        <xsl:if test="@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="nameIdentifierScheme">GND</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://d-nb.info/gnd/</xsl:attribute>
                                <xsl:value-of select="@authority" />
                            </nameIdentifier>
                        </xsl:if>
                        <xsl:if test="//dspace:field[@mdschema='item' and @element='dataciteContributorCorpROR'][number($i)]/@authority!=''">
                            <nameIdentifier>
                                <xsl:attribute name="schemeURI">https://ror.org/</xsl:attribute>
                                <xsl:attribute name="nameIdentifierScheme">ROR</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='item' and @element='dataciteContributorCorpROR'][number($i)]/@authority" />
                            </nameIdentifier>
                        </xsl:if>
                    </contributor>
                </xsl:if>
                </xsl:for-each>
            </xsl:if>
    </xsl:template>

    <!-- 
        DataCite (8), DataCite (8.1)
        Adds Date and dateType information
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='date' and
                        (@qualifier='accessioned'
                         or @qualifier='available'
                         or @qualifier='copyright'
                         or @qualifier='created'
                         or @qualifier='issued'
                         or @qualifier='submitted'
                         or @qualifier='updated')]">
        <xsl:if test="@qualifier='accessioned'
                         or @qualifier='available'
                         or @qualifier='copyright'
                         or @qualifier='created'
                         or @qualifier='issued'
                         or @qualifier='submitted'
                         or @qualifier='updated'">
            <xsl:element name="date">
                <xsl:if test="@qualifier='accessioned'">
                    <xsl:attribute name="dateType">Accepted</xsl:attribute>
                </xsl:if>
                <xsl:if test="@qualifier='issued'">
                    <xsl:attribute name="dateType">Issued</xsl:attribute>
                </xsl:if>
                <!-- DSpace recommends to use dc.date.submitted for theses and/or
                     dissertations. DataCite uses submitted for the "date the.
                     creator submits the resource to the publisher". -->
                <xsl:if test="@qualifier='submitted'">
                    <xsl:attribute name="dateType">Issued</xsl:attribute>
                </xsl:if>
                <xsl:if test="@qualifier='available'">
                    <xsl:attribute name="dateType">Available</xsl:attribute>
                </xsl:if>
                <xsl:if test="@qualifier='copyright'">
                    <xsl:attribute name="dateType">Copyrighted</xsl:attribute>
                </xsl:if>
                <xsl:if test="@qualifier='created'">
                    <xsl:attribute name="dateType">Created</xsl:attribute>
                </xsl:if>
                <xsl:if test="@qualifier='updated'">
                    <xsl:attribute name="dateType">Updated</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="substring(., 1, 10)" />
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <!-- 
        DataCite (9)
        Adds Language information
        Transforming the language flags according to IETF BCP 47 or ISO 639-1
    -->
     <xsl:template match="//dspace:field[@mdschema='dc' and @element='language' and (@qualifier='iso' or @qualifier='rfc3066')][1]">
        <xsl:element name="language">
            <xsl:choose>
                <xsl:when test="contains(string(text()), '_')">
                    <xsl:value-of select="translate(string(text()), '_', '-')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="string(text())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (10), DataCite (10.1)
        Adds resourceType and resourceTypeGeneral information
    -->
        <xsl:template match="//dspace:field[@mdschema='datacite' and @element='resourceTypeGeneral']">
            <!-- Transforming the language flags according to ISO 639-2/B & ISO 639-3 -->
            <xsl:element name="resourceType">
                <xsl:attribute name="resourceTypeGeneral">
                    <xsl:value-of select="." />
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='datacite' and @element='resourceType']">
                        <xsl:value-of select="//dspace:field[@mdschema='datacite' and @element='resourceType']" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='type' and @qualifier='casrai']" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
    </xsl:template>

    <!--
        DataCite (11), DataCite (11.1) 
        Adds AlternativeIdentifier and alternativeIdentifierType information
        Adds all identifiers except the doi.

        This element is important as it is used to recognize for which DSpace
        objet a DOI is reserved for. The DataCiteConnector will test all
        AlternativeIdentifiers by using HandleManager.
        resolveUrlToHandle(context, altId) until one is recognized or all have
        been tested.
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='identifier' and not(@qualifier='issn') and not(@qualifier='scopus') and @qualifier and not(starts-with(., concat('http://dx.doi.org/', $prefix)))]">
        <xsl:element name="alternateIdentifier">
            <xsl:if test="@qualifier">
                <xsl:attribute name="alternateIdentifierType"><xsl:value-of select="@qualifier" /></xsl:attribute>
            </xsl:if>
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!--
        DataCite (12), DataCite (12.1).
        Adds RelatedIdentifier and relatedIdentifierType information
    -->
    <xsl:template match="//dspace:field[@mdschema='tuhh' and @element='relation' and @qualifier='issupplementedby']">
        <xsl:element name="relatedIdentifier">
            <xsl:choose>
                <xsl:when test="starts-with(substring-before(., ':'), 'http')">
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:text>URL</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="relationType">IsSupplementedBy</xsl:attribute>
                    <xsl:value-of select="." />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:choose>
                            <xsl:when test="substring-before(., ':')='hdl'">
                                <xsl:text>Handle</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='doi'">
                                <xsl:text>DOI</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='urn'">
                                <xsl:text>URN</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='isbn'">
                                <xsl:text>ISBN</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="substring-before(., ':')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:attribute name="relationType">IsSupplementedBy</xsl:attribute>
                    <xsl:value-of select="substring-after(., ':')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    <xsl:template match="//dspace:field[@mdschema='datacite' and @element='relation' and @qualifier!='IsIdenticalTo']">
        <xsl:if test="contains(., ':') and (substring-before(., ':')='hdl' or substring-before(., ':')='doi' or substring-before(., ':')='urn' or substring-before(., ':')='http' or substring-before(., ':')='ARK' or substring-before(., ':')='arXiv' or substring-before(., ':')='arxiv')">
<!--
bibcode
EAN13
EISSN
IGSN
ISBN
ISSN
ISTC
LISSN
LSID
PMID
PURL
UPC
URL
w3id
-->
        <xsl:element name="relatedIdentifier">
            <xsl:choose>
                <xsl:when test="starts-with(substring-before(., ':'), 'http')">
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:text>URL</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="relationType"><xsl:value-of select="@qualifier" /></xsl:attribute>
                    <xsl:value-of select="." />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:choose>
                            <xsl:when test="substring-before(., ':')='hdl'">
                                <xsl:text>Handle</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='doi'">
                                <xsl:text>DOI</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='urn'">
                                <xsl:text>URN</xsl:text>
                            </xsl:when>
                            <xsl:when test="substring-before(., ':')='arxiv'">
                                <xsl:text>arXiv</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="substring-before(., ':')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:attribute name="relationType"><xsl:value-of select="@qualifier" /></xsl:attribute>
                    <xsl:value-of select="substring-after(., ':')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        </xsl:if>
    </xsl:template>
    <xsl:template match="//dspace:field[@mdschema='tuhh' and @element='publisher']">
        <xsl:element name="relatedIdentifier">
            <xsl:choose>
                <xsl:when test="starts-with(substring-before(., ':'), 'http')">
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:text>URL</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="relationType">
                        <xsl:choose>
                            <xsl:when test="//dspace:field[@mdschema='local' and @element='type' and @qualifier='version']='publishedVersion'">
                                <xsl:text>IsIdenticalTo</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>IsVariantFormOf</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="." />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="relatedIdentifierType">
                        <xsl:text>DOI</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="relationType">
                        <xsl:choose>
                            <xsl:when test="//dspace:field[@mdschema='local' and @element='type' and @qualifier='version']='publishedVersion'">
                                <xsl:text>IsIdenticalTo</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>IsVariantFormOf</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="." />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (13)
        Adds Size information
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='format' and @qualifier='extent']">
        <xsl:element name="size">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (14)
        Adds Format information
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='format'][not(@qualifier='extent')]">
        <xsl:element name="format">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (16)
        Adds Rights information
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='rights' and @qualifier='cc']">
        <xsl:element name="rights">
            <xsl:attribute name="rightsIdentifierScheme">
                <xsl:text>SPDX</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="schemeURI">
                <xsl:text>https://spdx.org/licenses/</xsl:text>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test=".='cc-null'">
                    <xsl:attribute name="rightsIdentifier">
                        <xsl:text>CC0-1.0</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="rightsURI">
                        <xsl:text>https://creativecommons.org/share-your-work/public-domain/cc0/</xsl:text>
                    </xsl:attribute>
                    <xsl:text>CC-0</xsl:text>
                </xsl:when>
                <xsl:when test=".='0'">
                    <xsl:attribute name="rightsIdentifier">
                        <xsl:text>CC0-1.0</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="rightsURI">
                        <xsl:text>https://creativecommons.org/share-your-work/public-domain/cc0/</xsl:text>
                    </xsl:attribute>
                    <xsl:text>CC-0</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="//dspace:field[@mdschema='dc' and @element='rights' and @qualifier='ccversion']">
                            <xsl:attribute name="rightsIdentifier">
                                <xsl:text>CC-</xsl:text><xsl:value-of select="translate(.,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" /><xsl:text>-</xsl:text><xsl:value-of select="//dspace:field[@mdschema='dc' and @element='rights' and @qualifier='ccversion']" />
                            </xsl:attribute>
                            <xsl:attribute name="rightsURI">
                                <xsl:text>https://creativecommons.org/licenses/</xsl:text><xsl:value-of select="." /><xsl:text>/</xsl:text><xsl:value-of select="//dspace:field[@mdschema='dc' and @element='rights' and @qualifier='ccversion']" />
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="rightsIdentifier">
                                <xsl:choose>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/4.0/'">
                                        <xsl:text>CC-BY-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/4.0/'">
                                        <xsl:text>CC-BY-SA-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/4.0/'">
                                        <xsl:text>CC-BY-ND-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/4.0/'">
                                        <xsl:text>CC-BY-NC-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/4.0/'">
                                        <xsl:text>CC-BY-NC-SA-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/4.0/'">
                                        <xsl:text>CC-BY-NC-ND-4.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/3.0/'">
                                        <xsl:text>CC-BY-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/3.0/'">
                                        <xsl:text>CC-BY-SA-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/3.0/'">
                                        <xsl:text>CC-BY-ND-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/3.0/'">
                                        <xsl:text>CC-BY-NC-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/3.0/'">
                                        <xsl:text>CC-BY-NC-SA-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/3.0/'">
                                        <xsl:text>CC-BY-NC-ND-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/3.0/de/'">
                                        <xsl:text>CC-BY-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/3.0/de/'">
                                        <xsl:text>CC-BY-SA-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/3.0/de/'">
                                        <xsl:text>CC-BY-ND-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/3.0/de/'">
                                        <xsl:text>CC-BY-NC-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/3.0/de/'">
                                        <xsl:text>CC-BY-NC-SA-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/3.0/de/'">
                                        <xsl:text>CC-BY-NC-ND-3.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/2.5/'">
                                        <xsl:text>CC-BY-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/2.5/'">
                                        <xsl:text>CC-BY-SA-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/2.5/'">
                                        <xsl:text>CC-BY-ND-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/2.5/'">
                                        <xsl:text>CC-BY-NC-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/2.5/'">
                                        <xsl:text>CC-BY-NC-SA-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/2.5/'">
                                        <xsl:text>CC-BY-NC-ND-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/2.5/de/'">
                                        <xsl:text>CC-BY-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/2.5/de/'">
                                        <xsl:text>CC-BY-SA-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/2.5/de/'">
                                        <xsl:text>CC-BY-ND-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/2.5/de/'">
                                        <xsl:text>CC-BY-NC-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/2.5/de/'">
                                        <xsl:text>CC-BY-NC-SA-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/2.5/de/'">
                                        <xsl:text>CC-BY-NC-ND-2.5</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/2.0/'">
                                        <xsl:text>CC-BY-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/2.0/'">
                                        <xsl:text>CC-BY-SA-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/2.0/'">
                                        <xsl:text>CC-BY-ND-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/2.0/'">
                                        <xsl:text>CC-BY-NC-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/2.0/'">
                                        <xsl:text>CC-BY-NC-SA-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/2.0/'">
                                        <xsl:text>CC-BY-NC-ND-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/2.0/de/'">
                                        <xsl:text>CC-BY-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/2.0/de/'">
                                        <xsl:text>CC-BY-SA-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/2.0/de/'">
                                        <xsl:text>CC-BY-ND-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/2.0/de/'">
                                        <xsl:text>CC-BY-NC-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/2.0/de/'">
                                        <xsl:text>CC-BY-NC-SA-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/2.0/de/'">
                                        <xsl:text>CC-BY-NC-ND-2.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/1.0/'">
                                        <xsl:text>CC-BY-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/1.0/'">
                                        <xsl:text>CC-BY-SA-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/1.0/'">
                                        <xsl:text>CC-BY-ND-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/1.0/'">
                                        <xsl:text>CC-BY-NC-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/1.0/'">
                                        <xsl:text>CC-BY-NC-SA-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/1.0/'">
                                        <xsl:text>CC-BY-NC-ND-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by/1.0/de/'">
                                        <xsl:text>CC-BY-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-sa/1.0/de/'">
                                        <xsl:text>CC-BY-SA-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nd/1.0/de/'">
                                        <xsl:text>CC-BY-ND-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc/1.0/de/'">
                                        <xsl:text>CC-BY-NC-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-sa/1.0/de/'">
                                        <xsl:text>CC-BY-NC-SA-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/licenses/by-nc-nd/1.0/de/'">
                                        <xsl:text>CC-BY-NC-ND-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/publicdomain/zero/1.0/'">
                                        <xsl:text>CC0-1.0</xsl:text>
                                    </xsl:when>
                                    <xsl:when test=".='https://creativecommons.org/publicdomain/zero/1.0/de/'">
                                        <xsl:text>CC0-1.0</xsl:text>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:attribute name="rightsURI">
                                <xsl:value-of select="." />
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
<!--
                    <xsl:attribute name="rightsURI">
                        <xsl:value-of select="." />
                    </xsl:attribute>
                    <xsl:text>CC-</xsl:text><xsl:value-of select="." /><xsl:text>-</xsl:text><xsl:value-of select="//dspace:field[@mdschema='dc' and @element='rights' and @qualifier='ccversion']" />
-->
                    <xsl:choose>
                        <xsl:when test="//dspace:field[@mdschema='dc' and @element='rights'][not(@qualifier)]">
                            <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='rights'][not(@qualifier)]" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="." />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='rights'][not(@qualifier='cc' or @qualifier='ccversion' or @qualifier='nationallicense') and @qualifier]">
        <xsl:if test="not(contains(., 'info:eu-repo/semantics'))">
            <xsl:element name="rights">
                <xsl:attribute name="rightsURI">
                    <xsl:value-of select="." />
                </xsl:attribute>
                <xsl:value-of select="." />
            </xsl:element>
        </xsl:if>
    </xsl:template>
    <!-- RULES:
        mixedopen, open = info:eu-repo/semantics/openAccess
        embargo_ = info:eu-repo/semantics/embargoedAccess
        restricted, embargo_restricted_, mixedrestricted = info:eu-repo/semantics/restrictedAccess
        reserved = info:eu-repo/semantics/closedAccess -->
    <xsl:template match="//dspace:field[@mdschema='item' and @element='grantfulltext']">
        <xsl:element name="rights">
            <xsl:choose>
                <xsl:when test="contains(., 'open')">
                    <xsl:attribute name="rightsURI">info:eu-repo/semantics/openAccess</xsl:attribute>
                </xsl:when>
                <xsl:when test="contains(., 'embargo_')">
                    <xsl:attribute name="rightsURI">info:eu-repo/semantics/embargoedAccess</xsl:attribute>
                </xsl:when>
                <xsl:when test="contains(., 'restricted')">
                    <xsl:attribute name="rightsURI">info:eu-repo/semantics/restrictedAccess</xsl:attribute>
                </xsl:when>
                <xsl:when test="contains(., 'reserved')">
                    <xsl:attribute name="rightsURI">info:eu-repo/semantics/closedAccess</xsl:attribute>
                </xsl:when>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (17)
        Description
    -->
    <xsl:template match="//dspace:field[@mdschema='dc' and @element='description' and (@qualifier='abstract' or @qualifier='tableofcontents' or not(@qualifier))]">
        <xsl:element name="description">
            <xsl:attribute name="descriptionType">
           	<xsl:choose>           
                    <xsl:when test="@qualifier='tableofcontents'">TableOfContents</xsl:when>
                    <xsl:when test="@qualifier='abstract'">Abstract</xsl:when>
                    <xsl:otherwise>Other</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- 
        DataCite (19)
        Funding Reference
    -->
    <xsl:template name="fundingReference">
        <xsl:variable name="funders">
            <xsl:for-each select="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funder']">
                <xsl:value-of select="." /><xsl:text> </xsl:text>
            </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funder']">
            <xsl:variable name="f">
                <xsl:number value="position()" />
            </xsl:variable>
            <xsl:if test=".!=$placeholder">
            <fundingReference>
                <funderName>
                    <xsl:value-of select="." />
                </funderName>
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderid'][number($f)]!='' and //dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderid'][number($f)]!=$placeholder">
                        <funderIdentifier>
                            <xsl:attribute name="funderIdentifierType">Crossref Funder ID</xsl:attribute>
                            <xsl:attribute name="schemeURI">https://www.crossref.org/services/funder-registry/</xsl:attribute>
                            <xsl:text>https://doi.org/10.13039/</xsl:text><xsl:value-of select="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderid'][number($f)]" />
                        </funderIdentifier>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderrorid'][number($f)]!='' and //dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderrorid'][number($f)]!=$placeholder">
                            <funderIdentifier>
                                <xsl:attribute name="funderIdentifierType">ROR</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://ror.org</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='funderrorid'][number($f)]" />
                            </funderIdentifier>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='grantno'][number($f)]!='' and //dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='grantno'][number($f)]!=$placeholder">
                    <awardNumber>
                        <xsl:value-of select="//dspace:field[@mdschema='crisitem' and @element='project' and @qualifier='grantno'][number($f)]" />
                    </awardNumber>
                </xsl:if>
                <xsl:if test="//dspace:field[@mdschema='dc' and @element='relation' and @qualifier='project'][number($f)]!='' and //dspace:field[@mdschema='dc' and @element='relation' and @qualifier='project'][number($f)]!=$placeholder">
                    <awardTitle>
                        <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='relation' and @qualifier='project'][number($f)]" />
                    </awardTitle>
                </xsl:if>
            </fundingReference>
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="//dspace:field[@mdschema='dc' and @element='description' and @qualifier='sponsorship']">
            <xsl:variable name="g">
                <xsl:number value="position()" />
            </xsl:variable>
            <xsl:if test=".!=$placeholder and not(contains($funders,.))">
            <fundingReference>
                <funderName>
                    <xsl:value-of select="." />
                </funderName>
                <xsl:choose>
                    <xsl:when test="//dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderid'][number($g)]!='' and //dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderid'][number($g)]!=$placeholder">
                        <funderIdentifier>
                            <xsl:attribute name="funderIdentifierType">Crossref Funder ID</xsl:attribute>
                            <xsl:attribute name="schemeURI">https://www.crossref.org/services/funder-registry/</xsl:attribute>
                            <xsl:text>https://doi.org/10.13039/</xsl:text><xsl:value-of select="//dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderid'][number($g)]" />
                        </funderIdentifier>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="//dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderrorid'][number($g)]!='' and //dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderrorid'][number($g)]!=$placeholder">
                            <funderIdentifier>
                                <xsl:attribute name="funderIdentifierType">ROR</xsl:attribute>
                                <xsl:attribute name="schemeURI">https://ror.org</xsl:attribute>
                                <xsl:value-of select="//dspace:field[@mdschema='crisitem' and @element='funder' and @qualifier='funderrorid'][number($g)]" />
                            </funderIdentifier>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </fundingReference>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="//dspace:field[@mdschema='dc' and @element='relation' and @qualifier='ispartof']">
        <xsl:element name="relatedItem">
            <xsl:attribute name="relationType">IsPublishedIn</xsl:attribute>
            <xsl:attribute name="relatedItemType">Journal</xsl:attribute>
            <xsl:element name="relatedItemIdentifier">
                <xsl:attribute name="relatedItemIdentifierType">ISSN</xsl:attribute>
                <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='identifier' and @qualifier='issn']" />
            </xsl:element>
            <titles>
                <title><xsl:value-of select="." /></title>
            </titles>
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='date' and @qualifier='issued']">
                <xsl:element name="publicationYear">
                    <xsl:value-of select="substring(//dspace:field[@mdschema='dc' and @element='date' and @qualifier='issued'], 1, 4)" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='issue']">
                <xsl:element name="issue">
                    <xsl:value-of select="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='issue']" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='volume']">
                <xsl:element name="volume">
                    <xsl:value-of select="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='volume']" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='startpage']">
                <xsl:element name="startpage">
                    <xsl:value-of select="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='startpage']" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='endpage']">
                <xsl:element name="endpage">
                    <xsl:value-of select="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='endpage']" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='articlenumber']">
                <xsl:element name="number">
                    <xsl:value-of select="//dspace:field[@mdschema='tuhh' and @element='container' and @qualifier='articlenumber']" />
                </xsl:element>
            </xsl:if>
            <xsl:if test="//dspace:field[@mdschema='dc' and @element='publisher']">
                <xsl:element name="publisher">
                    <xsl:value-of select="//dspace:field[@mdschema='dc' and @element='publisher']" />
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
