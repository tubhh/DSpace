<?xml version="1.0" encoding="UTF-8" ?>
<!-- 


    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/
	Developed by DSpace @ Lyncode <dspace@lyncode.com>
	
	> http://www.openarchives.org/OAI/2.0/oai_dc.xsd

 -->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://www.lyncode.com/xoai"
	version="1.0">
	<xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />
	
	<xsl:template match="/">
		<oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
			xmlns:dc="http://purl.org/dc/elements/1.1/" 
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
			xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
			<!-- dc.title -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
				<dc:title><xsl:value-of select="." /></dc:title>
			</xsl:for-each>
			<!-- dc.title.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:element/doc:field[@name='value']">
				<dc:title><xsl:value-of select="." /></dc:title>
			</xsl:for-each>
			<!-- dc.creator -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='creator']/doc:element/doc:field[@name='value']">
				<dc:creator><xsl:value-of select="." /></dc:creator>
			</xsl:for-each>
			<!-- dc.contributor.author -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
				<dc:creator><xsl:value-of select="." /></dc:creator>
			</xsl:for-each>
			<!-- dc.contributor.editor -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='editor']/doc:element/doc:field[@name='value']">
				<dc:creator><xsl:value-of select="." /></dc:creator>
			</xsl:for-each>
			<!-- dc.contributor.* (!author) -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name!='author' and @name!='editor']/doc:element/doc:field[@name='value']">
				<dc:contributor><xsl:value-of select="." /></dc:contributor>
			</xsl:for-each>
			<!-- dc.contributor -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element/doc:field[@name='value']">
				<dc:contributor><xsl:value-of select="." /></dc:contributor>
			</xsl:for-each>
			<!-- dc.subject -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
				<dc:subject><xsl:value-of select="." /></dc:subject>
			</xsl:for-each>
			<!-- dc.subject.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:element/doc:field[@name='value']">
				<dc:subject><xsl:value-of select="." /></dc:subject>
			</xsl:for-each>
                        <!-- DINI Certificate 2013: marking ddc code field -->
                        <!-- dc.subject.ddccode -->
                        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='ddccode']/doc:element/doc:field[@name='value']">
                            <dc:subject><xsl:text>ddc:</xsl:text><xsl:value-of select="." /></dc:subject>
                        </xsl:for-each>
<!--
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element">
                            <xsl:choose>
                                <xsl:when test="@name='ddccode'">
                                    <dc:subject><xsl:text>ddc:</xsl:text><xsl:value-of select="./doc:element/doc:field[@name='value']" /></dc:subject>
                                </xsl:when>
                                <xsl:otherwise>
                                    <dc:subject><xsl:value-of select="./doc:element/doc:field[@name='value']" /></dc:subject>
                                </xsl:otherwise>
                            </xsl:choose>
			</xsl:for-each>
-->
			<!-- dc.description -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element/doc:field[@name='value']">
				<dc:description><xsl:value-of select="." /></dc:description>
			</xsl:for-each>
			<!-- dc.description.* (not provenance)-->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name!='provenance']/doc:element/doc:field[@name='value']">
				<dc:description><xsl:value-of select="." /></dc:description>
			</xsl:for-each>
			<!-- dc.date -->
                        <!--
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element/doc:field[@name='value']">
				<dc:date><xsl:value-of select="." /></dc:date>
			</xsl:for-each>
                        -->
			<!-- dc.date.* -->
                        <!--
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element/doc:element/doc:field[@name='value']">
				<dc:date><xsl:value-of select="." /></dc:date>
			</xsl:for-each>
                        -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                            <dc:date>
                                <xsl:choose>
                                    <xsl:when test="string-length(.) = 4">
                                        <xsl:value-of select="." /><xsl:text>-01-01</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </dc:date>
			</xsl:for-each>
                        <!-- DINI-Zertifikat 2013: nur ein date-Element. Wir nehmen das dc.date.issued (s.o.)
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
				<dc:date><xsl:value-of select="substring-before(., 'T')" /></dc:date>
			</xsl:for-each>
                        -->
			<!-- dc.type -->
			<xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='openairetype']/doc:element/doc:field[@name='value']">
<!--			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']"> -->
				<dc:type><xsl:value-of select="." /></dc:type>
			</xsl:for-each>
			<!-- dc.type.* -->
                        <!-- fight the type chaos: only show dc.type, not all of the subtypes
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:element/doc:field[@name='value']">
				<dc:type><xsl:value-of select="." /></dc:type>
			</xsl:for-each>
                        -->
                        <!-- DINI Certificate 2013: dini type field -->
                        <!-- dc.type.dini -->
                        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='dini']/doc:element/doc:field[@name='value']">
                            <dc:type><xsl:text>doc-type:</xsl:text><xsl:value-of select="." /></dc:type>
                        </xsl:for-each>
                        <!-- TUHH identifier first -->
                        <xsl:for-each select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                            <dc:identifier><xsl:text>http://dx.doi.org/</xsl:text><xsl:value-of select="." /></dc:identifier>
                        </xsl:for-each>
			<!-- dc.identifier -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element/doc:field[@name='value']">
				<dc:identifier><xsl:value-of select="." /></dc:identifier>
			</xsl:for-each>
			<!-- dc.identifier.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name!='citation']/doc:element/doc:field[@name='value']">
				<dc:identifier><xsl:value-of select="." /></dc:identifier>
			</xsl:for-each>
			<!-- dc.language -->
<!--
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:field[@name='value']">
                            <xsl:variable name="lang"><xsl:value-of select="." /></xsl:variable>
                            <xsl:choose>
                                <xsl:when test="$lang='en'">
                                    <dc:language><xsl:text>eng</xsl:text></dc:language>
                                </xsl:when>
                                <xsl:when test="$lang='de'">
                                    <dc:language><xsl:text>ger</xsl:text></dc:language>
                                </xsl:when>
                                <xsl:otherwise>
                                    <dc:language><xsl:value-of select="." /></dc:language>
                                </xsl:otherwise>
                            </xsl:choose>
			</xsl:for-each>
-->
			<!-- dc.language.* -->
<!--
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value']">
                            <xsl:choose>
                                <xsl:when test=".='en'">
                                    <dc:language><xsl:text>eng</xsl:text></dc:language>
                                </xsl:when>
                                <xsl:when test=".='de'">
                                    <dc:language><xsl:text>ger</xsl:text></dc:language>
                                </xsl:when>
                                <xsl:otherwise>
                                    <dc:language><xsl:value-of select="." /></dc:language>
                                </xsl:otherwise>
                            </xsl:choose>
-->
			<xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='languageiso639-1']/doc:element/doc:field[@name='value']">
				<dc:language><xsl:value-of select="." /></dc:language>
			</xsl:for-each>
			<!-- dc.relation (from the CRIS Project) -->
			<xsl:for-each select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='project']/doc:element[@name='openAire']/doc:element/doc:field[@name='value']">
				<dc:relation><xsl:value-of select="." /></dc:relation>
			</xsl:for-each>
			<!-- dc.relation (flat metadata) -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:field[@name='value']">
				<dc:relation><xsl:value-of select="." /></dc:relation>
			</xsl:for-each>
			<!-- dc.relation.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:element/doc:field[@name='value']">
				<dc:relation><xsl:value-of select="." /></dc:relation>
			</xsl:for-each>
			<!-- dc.rights -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[@name='value']">
				<dc:rights><xsl:value-of select="." /></dc:rights>
			</xsl:for-each>
                        <!-- select all rights -->
                        <!-- RULES:
                            mixedopen, open = info:eu-repo/semantics/openAccess
                            embargo_ = info:eu-repo/semantics/embargoedAccess
                            restricted, embargo_restricted_, mixedrestricted = info:eu-repo/semantics/restrictedAccess
                            reserved = info:eu-repo/semantics/closedAccess -->
                        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='grantfulltext']//doc:field[@name='value']">
                            <xsl:choose>
                                <xsl:when test="contains(., 'open')">
                                    <dc:rights>info:eu-repo/semantics/openAccess</dc:rights>
                                </xsl:when>
                                <xsl:when test="contains(., 'embargo_')">
                                    <dc:rights>info:eu-repo/semantics/embargoedAccess"</dc:rights>
                                    <dc:date><xsl:text>info:eu-repo/date/embargoEnd/</xsl:text><xsl:value-of select="substring(., 9, 4)"/><xsl:text>-</xsl:text><xsl:value-of select="substring(., 13, 2)"/><xsl:text>-</xsl:text><xsl:value-of select="substring(., 15, 2)"/></dc:date>
                                </xsl:when>
                                <xsl:when test="contains(., 'restricted')">
                                    <dc:rights>info:eu-repo/semantics/restrictedAccess</dc:rights>
                                </xsl:when>
                                <xsl:when test="contains(., 'reserved')">
                                    <dc:rights>info:eu-repo/semantics/closedAccess</dc:rights>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:for-each>

			<!-- dc.date for embargo enddate -->
			<xsl:for-each select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='date']/doc:element[@name='embargo']/doc:element/doc:field[@name='value']">
				<dc:date>info:eu-repo/date/embargoEnd/<xsl:value-of select="." /></dc:date>
			</xsl:for-each>
			<!-- dc.rights.uri -->
                        <!-- dc.rights.cc -->
                        <xsl:choose>
                            <xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='cc']/doc:element/doc:field[@name='value']">
                                <dc:rights>https://creativecommons.org/licenses/<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='cc']/doc:element/doc:field[@name='value']" />/<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='ccversion']/doc:element/doc:field[@name='value']" /></dc:rights>
                            </xsl:when>
                            <xsl:otherwise>
			        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
				    <dc:rights><xsl:value-of select="." /></dc:rights>
			        </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
			<!-- dc.rights OpenAire-->
<!--
			<xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='grantfulltext']/doc:element/doc:field[@name='value']">
				<xsl:choose>
					<xsl:when test="contains(., 'embargoEnd')">
						<dc:rights>info:eu-repo/semantics/embargoedAccess</dc:rights>
						<dc:date><xsl:value-of select="." /></dc:date>
					</xsl:when>
					<xsl:otherwise>
						<dc:rights><xsl:value-of select="." /></dc:rights>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
-->
			<!-- dc.format -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element/doc:field[@name='value']">
				<dc:format><xsl:value-of select="." /></dc:format>
			</xsl:for-each>
			<!-- dc.format.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='format']/doc:element/doc:element/doc:field[@name='value']">
				<dc:format><xsl:value-of select="." /></dc:format>
			</xsl:for-each>
			<!-- ? -->
			<xsl:for-each select="doc:metadata/doc:element[@name='bitstreams']/doc:element[@name='bitstream']/doc:field[@name='format']">
				<dc:format><xsl:value-of select="." /></dc:format>
			</xsl:for-each>
			<!-- dc.coverage -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='coverage']/doc:element/doc:field[@name='value']">
				<dc:coverage><xsl:value-of select="." /></dc:coverage>
			</xsl:for-each>
			<!-- dc.coverage.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='coverage']/doc:element/doc:element/doc:field[@name='value']">
				<dc:coverage><xsl:value-of select="." /></dc:coverage>
			</xsl:for-each>
			<!-- dc.publisher -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:field[@name='value']">
				<dc:publisher><xsl:value-of select="." /></dc:publisher>
			</xsl:for-each>
			<!-- dc.publisher.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:element/doc:field[@name='value']">
				<dc:publisher><xsl:value-of select="." /></dc:publisher>
			</xsl:for-each>
			<!-- dc.source -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element/doc:field[@name='value']">
				<dc:source><xsl:value-of select="." /></dc:source>
			</xsl:for-each>
			<!-- dc.source.* -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element/doc:element/doc:field[@name='value']">
				<dc:source><xsl:value-of select="." /></dc:source>
			</xsl:for-each>
		</oai_dc:dc>
	</xsl:template>
</xsl:stylesheet>
