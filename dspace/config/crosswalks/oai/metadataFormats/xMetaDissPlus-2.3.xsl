<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://www.lyncode.com/xoai" 
    xmlns:str="xalan://java.lang.String"
    version="1.0">

        <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />

        <!-- global variables-->
        <xsl:variable name="handle-prefix">http://tore.tuhh.de/handle/11420/</xsl:variable>
        <xsl:variable name="handle-prefix-pure">11420/</xsl:variable>
        <xsl:variable name="gkdnr">1097763-6</xsl:variable>
        <xsl:variable name="dnbnr">F6000-0198</xsl:variable>

	<xsl:variable name="lang">
		<xsl:choose>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'deu'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'de'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'de_DE'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'en'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'en_US'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value'] = 'deu'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value'] = 'de'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value'] = 'de_DE'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value'] = 'en'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value'] = 'en_US'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element[@name='iso']/doc:element/doc:field[@name='value']" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
        <xsl:variable name="publType">
            <xsl:choose>
                <xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='thesis']/doc:element/doc:field[@name='value']">
                    <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='thesis']/doc:element/doc:field[@name='value']" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="mainPublType">
            <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
        </xsl:variable>

	<xsl:template match="/">
		 <xMetaDiss:xMetaDiss xmlns:xMetaDiss="http://www.d-nb.de/standards/xmetadissplus/"
		    xmlns:dc="http://purl.org/dc/elements/1.1/" 
		    xmlns:dcterms="http://purl.org/dc/terms/" 
			xmlns:cc="http://www.d-nb.de/standards/cc/"  
			xmlns:dcmitype="http://purl.org/dc/dcmitype/" 
			xmlns:pc="http://www.d-nb.de/standards/pc/" 
			xmlns:urn="http://www.d-nb.de/standards/urn/" 
			xmlns:hdl="http://www.d-nb.de/standards/hdl/" 
			xmlns:doi="http://www.d-nb.de/standards/doi/" 
			xmlns:thesis="http://www.ndltd.org/standards/metadata/etdms/1.0/" 
			xmlns:ddb="http://www.d-nb.de/standards/ddb/" 
			xmlns:dini="http://www.d-nb.de/standards/xmetadissplus/type/" 
			xmlns="http://www.d-nb.de/standards/subject/" 
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
			xsi:schemaLocation="http://www.d-nb.de/standards/xmetadissplus/ http://files.dnb.de/standards/xmetadissplus/xmetadissplus.xsd">

			<!-- title data: dc.title, dc.title.alternative -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']">
				<dc:title xsi:type="ddb:titleISO639-2" lang="{$lang}">
					<xsl:value-of select="doc:element/doc:field[@name='value']" />
				</dc:title>
				<xsl:if test="doc:element[@name='alternative']/doc:element/@name">
					<dcterms:alternative xsi:type="ddb:talternativeISO639-2">
                                            <xsl:choose>
                                                <xsl:when test="doc:element[@name='alternative']/doc:element/@name = 'de'">
                                                    <xsl:attribute name="lang"><xsl:text>ger</xsl:text></xsl:attribute>
                                                </xsl:when>
                                                <xsl:when test="doc:element[@name='alternative']/doc:element/@name = 'de_DE'">
                                                    <xsl:attribute name="lang"><xsl:text>ger</xsl:text></xsl:attribute>
                                                </xsl:when>
                                                <xsl:when test="doc:element[@name='alternative']/doc:element/@name = 'en'">
                                                    <xsl:attribute name="lang"><xsl:text>eng</xsl:text></xsl:attribute>
                                                </xsl:when>
                                                <xsl:when test="doc:element[@name='alternative']/doc:element/@name = 'en_US'">
                                                    <xsl:attribute name="lang"><xsl:text>eng</xsl:text></xsl:attribute>
                                                </xsl:when>
                                            </xsl:choose>
                                            <xsl:value-of select="doc:element[@name='alternative']/doc:element/doc:field[@name='value']"/>
					</dcterms:alternative>
	        		</xsl:if>
    			</xsl:for-each>
                        <xsl:variable name="orcid" select="doc:metadata/doc:element[@name='item']/doc:element[@name='creatorOrcid']//doc:field[@name='authority']"/>
			<!-- author data: dc.contributor.author -->
			<xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='creatorGND']/doc:element/doc:field[@name='value']">
                            <xsl:variable name="i">
                                <xsl:number value="position()" />
                            </xsl:variable>
				<dc:creator xsi:type="pc:MetaPers">
					<pc:person>
                                            <xsl:if test="../doc:field[@name='authority'][number($i)]!=''">
                                                <xsl:attribute name="ddb:GND-Nr"><xsl:value-of select="../doc:field[@name='authority'][number($i)]" /></xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="../../../../doc:element[@name='item']/doc:element[@name='creatorOrcid']//doc:field[@name='authority'][number($i)]!=''">
                                                <ddb:ORCID><xsl:value-of select="../../../../doc:element[@name='item']/doc:element[@name='creatorOrcid']//doc:field[@name='authority'][number($i)]" /></ddb:ORCID>
                                            </xsl:if>
            					<pc:name type="nameUsedByThePerson">
							<!-- handle names with "von", "van", "Van", and "de" -->
                					<xsl:variable name="tail"><xsl:value-of select="normalize-space(substring-after(., ','))"/></xsl:variable>
							<xsl:variable name="prefix">
				 			<xsl:choose>
								<xsl:when test="contains($tail, ' von ') ">
									<xsl:value-of select="concat('von ', substring-after($tail, ' von '))"/>
								</xsl:when>
								<xsl:when test="contains($tail, ' van ') ">
									<xsl:value-of select="concat('van ', substring-after($tail, ' van '))"/>
								</xsl:when>
								<xsl:when test="contains($tail, ' Van ') ">
									<xsl:value-of select="concat('Van ', substring-after($tail, ' Van '))"/>
								</xsl:when>
								<xsl:when test="contains($tail, ' de ') ">
									<xsl:value-of select="concat('de ', substring-after($tail, ' de '))"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>none</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
							</xsl:variable>	
							<pc:foreName>
								<xsl:choose>
									<xsl:when test="($prefix != 'none')">
										<xsl:value-of select="substring-before($tail, $prefix)" />
									</xsl:when>
	                    						<xsl:otherwise>
										<xsl:value-of select="$tail"/>
	                    						</xsl:otherwise>
        	            					</xsl:choose>
                    					</pc:foreName>
                    					<pc:surName>
								<xsl:value-of select="substring-before(., ',')"/>
							</pc:surName>
                    					<xsl:if test="not($prefix = 'none')">
								<pc:prefix><xsl:value-of select="$prefix" /></pc:prefix>
                    					</xsl:if>
                				</pc:name>
            				</pc:person>
				</dc:creator>
			</xsl:for-each>
			<!-- subjects data: dc.subject.other-->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='other']/doc:element/doc:field[@name='value']">
				<dc:subject xsi:type="xMetaDiss:noScheme">
                                	<xsl:value-of select="."/>
                                </dc:subject>
			</xsl:for-each>
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
				<dc:subject xsi:type="xMetaDiss:noScheme">
                                	<xsl:value-of select="."/>
                                </dc:subject>
			</xsl:for-each>
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='ddccode']/doc:element/doc:field[@name='value']">
				<dc:subject xsi:type="xMetaDiss:DDC-SG">
                                    <xsl:value-of select="."/>
                                </dc:subject>
			</xsl:for-each>
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='classification']/doc:element/doc:field[@name='value']">
				<dc:subject xsi:type="xMetaDiss:SWD"><xsl:value-of select="."/></dc:subject>
			</xsl:for-each>

			<!-- abstract data: dc.description -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
				<dcterms:abstract xsi:type="ddb:contentISO639-2">
                                    <xsl:choose>
                                        <xsl:when test="../@name = 'de'">
                                            <xsl:attribute name="lang"><xsl:text>ger</xsl:text></xsl:attribute>
                                        </xsl:when>
                                        <xsl:when test="../@name = 'de_DE'">
                                            <xsl:attribute name="lang"><xsl:text>ger</xsl:text></xsl:attribute>
                                        </xsl:when>
                                        <xsl:when test="../@name = 'en'">
                                            <xsl:attribute name="lang"><xsl:text>eng</xsl:text></xsl:attribute>
                                        </xsl:when>
                                        <xsl:when test="../@name = 'en_US'">
                                            <xsl:attribute name="lang"><xsl:text>eng</xsl:text></xsl:attribute>
                                        </xsl:when>
                                    </xsl:choose>
					<xsl:value-of select="."/>
				</dcterms:abstract>
			</xsl:for-each>

			<!-- publisher: constant data -->
			<dc:publisher ddb:role="Universitaetsbibliothek" xsi:type="cc:Publisher" type="dcterms:ISO3166" countryCode="DE">
				<cc:universityOrInstitution cc:GKD-Nr="{$gkdnr}">
					<cc:name>Universitätsbibliothek der Technischen Universität Hamburg-Harburg</cc:name>
					<cc:place>Hamburg</cc:place>
				</cc:universityOrInstitution>
				<cc:address cc:Scheme="DIN5008">Denickestr. 22, 21071 Hamburg</cc:address>
			</dc:publisher>

			<!-- contributors data: dc.contributor.advisor -->
			<xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='advisorGND']/doc:element/doc:field[@name='value']">
                            <xsl:variable name="a">
                                <xsl:number value="position()" />
                            </xsl:variable>
				<dc:contributor xsi:type="pc:Contributor" countryCode="DE">
					<xsl:choose>
						<xsl:when test="contains(../../@name, 'eferee')">
							<xsl:attribute name="thesis:role">referee</xsl:attribute>
						</xsl:when>
						<xsl:when test="contains(../../@name, 'dvisor')">
							<xsl:attribute name="thesis:role">advisor</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="thesis:role"><xsl:value-of select="../../@name"/></xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<pc:person>
                                            <xsl:if test="../doc:field[@name='authority'][number($a)]!=''">
                                                <xsl:attribute name="ddb:GND-Nr"><xsl:value-of select="../doc:field[@name='authority'][number($a)]" /></xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="../../../../doc:element[@name='item']/doc:element[@name='advisorOrcid']//doc:field[@name='authority'][number($a)]!=''">
                                                <ddb:ORCID><xsl:value-of select="../../../../doc:element[@name='item']/doc:element[@name='advisorOrcid']//doc:field[@name='authority'][number($a)]" /></ddb:ORCID>
                                            </xsl:if>
						<xsl:variable name="tail" select="substring-after(., ',')"/>
						<!-- allowed academic titles: "Prof. Dr.", "PD Dr.", Prof. em.", "Dr.", "Prof. Dr.Dr.", "Prof. Dr. h.c.", "Dr. h.c." -->
						<xsl:choose>
							 <xsl:when test="contains(., 'Prof.')">
								<pc:name type="otherName">
									<pc:foreName><xsl:value-of select="normalize-space(substring-before($tail, 'Prof.'))"/></pc:foreName>
									<pc:surName><xsl:value-of select="substring-before(., ',')"/></pc:surName>
								</pc:name>
								<pc:academicTitle>
									<xsl:text>Prof.</xsl:text>
									<xsl:if test="contains($tail, 'Prof. em.')">
										<xsl:text> em.</xsl:text>
									</xsl:if>
								</pc:academicTitle>
								
							</xsl:when>
							<xsl:when test="contains(., 'PD')">
								<pc:name type="otherName">
									<pc:foreName><xsl:value-of select="normalize-space(substring-before($tail, 'PD'))"/></pc:foreName>
									<pc:surName><xsl:value-of select="substring-before(., ',')"/></pc:surName>
								</pc:name>
								<pc:academicTitle>PD</pc:academicTitle>
							</xsl:when>
							<xsl:otherwise>
								 <pc:name type="otherName">
									  <pc:foreName><xsl:value-of select="$tail"/></pc:foreName>
									  <pc:surName><xsl:value-of select="substring-before(., ',')"/></pc:surName>
								</pc:name>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:if test="contains($tail, 'Dr. Dr.')">
							<pc:academicTitle>Dr.</pc:academicTitle>
						</xsl:if>
						<xsl:if test="contains($tail, 'Dr.')">
							<pc:academicTitle>
								<xsl:text>Dr.</xsl:text>
								<xsl:if test="contains($tail, 'Dr. h.c.')">
									<xsl:text> h.c.</xsl:text>
								</xsl:if>
							</pc:academicTitle>
						</xsl:if>
					</pc:person>
				</dc:contributor>
			</xsl:for-each>

			<!-- dates: dcterms.dateAccepted, dc.date.issued -->
			<xsl:if test="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='dateAccepted']/doc:element/doc:field[@name='value']">
				<dcterms:dateAccepted xsi:type="dcterms:W3CDTF">
					<xsl:value-of select="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='dateAccepted']/doc:element/doc:field[@name='value']"/>
				</dcterms:dateAccepted>
			</xsl:if>
                        <xsl:choose>
                            <xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                                <dcterms:issued xsi:type="dcterms:W3CDTF">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']"/>
                                </dcterms:issued>
                            </xsl:when>
                            <xsl:otherwise>
                                <dcterms:issued xsi:type="dcterms:W3CDTF">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']"/>
                                </dcterms:issued>
                            </xsl:otherwise>
                        </xsl:choose>

			<!-- publication type: dc.type or (if that is set) dc.type.thesis -->
			<dc:type xsi:type="dini:PublType">
				<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element[@name='dini']/doc:element/doc:field[@name='value']" />
			</dc:type>
                        <xsl:if test="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='DCMIType']/doc:element/doc:field[@name='value']">
                            <dc:type xsi:type="dcterms:DCMIType">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='DCMIType']/doc:element/doc:field[@name='value']" />
                            </dc:type>
                        </xsl:if>

			<!-- driver version: static content -->
			<dini:version_driver>publishedVersion</dini:version_driver>

			<!-- identifier: dc.identifier.urn -->
                        <xsl:choose>
                            <xsl:when test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='identifier']/doc:element[@name='urn']/doc:element/doc:field[@name='value']">
                                <dc:identifier xsi:type="urn:nbn">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='identifier']/doc:element[@name='urn']/doc:element/doc:field[@name='value']"/>
                                </dc:identifier>
                            </xsl:when>
                            <xsl:otherwise>
                                <dc:identifier xsi:type="hdl:hdl">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='hdl']/doc:element/doc:field[@name='value']"/>
                                </dc:identifier>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- identifier: dc.identifier.doi -->
                        <!--
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                            <dc:identifier xsi:type="doi">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']"/>
                            </dc:identifier>
                        </xsl:if>
                        -->
                        <!-- identifier: dc.identifier.handle -->
                        <!--
                        <dc:identifier xsi:type="hdl">
                            http://hdl.handle.net/<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='hdl']/doc:element/doc:field[@name='value']"/>
                        </dc:identifier>
                        -->

                        <!-- External source of publication: dc.identifier.citation -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='citation']/doc:element/doc:field[@name='value']">
                            <dc:source xsi:type="ddb:noScheme">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='citation']/doc:element/doc:field[@name='value']"/>
                            </dc:source>
                        </xsl:if>

			<!-- language of publication: dc.language.iso -->
			<dc:language xsi:type="dcterms:ISO639-2">
				<xsl:value-of select="$lang"/>
			</dc:language>

			<!-- series: dc.relation.ispartofseries
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
                            <xsl:choose>
                                <xsl:when test="$mainPublType='article' or $mainPublType='Article' or $mainPublType='journal' or $mainPublType='Journal'">
                                    <dcterms:isPartOf xsi:type="ddb:ZSTitelID">
                                        <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='authority']"/>
                                    </dcterms:isPartOf>
                                    <dcterms:isPartOf xsi:type="ddb:ZS-Ausgabe">
                                        <xsl:value-of select="substring-after(doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value'], ';')"/>
                                    </dcterms:isPartOf>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                    <xsl:when test="contains(., ' ; ')">
                                        <dcterms:isPartOf xsi:type="ddb:noScheme">
                                            <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']"/>
                                        </dcterms:isPartOf>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <dcterms:isPartOf xsi:type="ddb:noScheme">
                                            <xsl:value-of select="substring-before(doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value'], ';')"/><xsl:text> ; </xsl:text><xsl:value-of select="substring-after(doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value'], ';')"/>
                                        </dcterms:isPartOf>
                                    </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                        -->

                        <!-- new series handling: take TUHH series ID from CRIS entity -->
                        <xsl:choose>
                            <xsl:when test="doc:metadata/doc:element[@name='item']/doc:element[@name='tuhhseriesid']/doc:element/doc:field[@name='authority']!='' and doc:metadata/doc:element[@name='item']/doc:element[@name='tuhhseriesid']/doc:element/doc:field[@name='authority']!='x'">
                                <dcterms:isPartOf xsi:type="ddb:ZSTitelID">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='item']/doc:element[@name='tuhhseriesid']/doc:element/doc:field[@name='authority']"/>
                                </dcterms:isPartOf>
                                <dcterms:isPartOf xsi:type="ddb:ZS-Ausgabe">
                                    <xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='relation']/doc:element[@name='ispartofseriesnumber']/doc:element/doc:field[@name='value']"/>
                                </dcterms:isPartOf>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="doc:metadata/doc:element[@name='item']/doc:element[@name='tuhhseriesid']/doc:element/doc:field[@name='authority']!='' and doc:metadata/doc:element[@name='item']/doc:element[@name='tuhhseriesid']/doc:element/doc:field[@name='authority']='x'">
                                <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
                                    <dcterms:isPartOf xsi:type="ddb:noScheme">
                                        <xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']"/><xsl:text> ; </xsl:text><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='relation']/doc:element[@name='ispartofseriesnumber']/doc:element/doc:field[@name='value']"/>
                                    </dcterms:isPartOf>
                                </xsl:if>
                                <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartof']/doc:element/doc:field[@name='value']">
                                    <dcterms:isPartOf xsi:type="ddb:noScheme">
                                        <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartof']/doc:element/doc:field[@name='value']"/>
                                        <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='container']/doc:element[@name='volume']/doc:element/doc:field[@name='value']">
                                            <xsl:text> ; </xsl:text>
                                            Volume <xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='container']/doc:element[@name='volume']/doc:element/doc:field[@name='value']"/>
                                            <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='container']/doc:element[@name='issue']/doc:element/doc:field[@name='value']">
                                                <xsl:text>, </xsl:text>
                                                Issue <xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='container']/doc:element[@name='issue']/doc:element/doc:field[@name='value']"/>
                                            </xsl:if>
                                        </xsl:if>
                                    </dcterms:isPartOf>
                                </xsl:if>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>

                        <!-- License terms: dc.rights -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
                            <dc:rights xsi:type="dcterms:URI">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']" />
                            </dc:rights>
                        </xsl:if>

			<!-- thesis degree: level mapping http://files.dnb.de/standards/xmetadiss/thesis.xsd -->
                        <xsl:if test="$mainPublType='Thesis'">
			<thesis:degree>
				<thesis:level>
					<xsl:choose>
						<xsl:when test="$publType = 'doctoralThesis'">
							<xsl:text>thesis.doctoral</xsl:text>
						</xsl:when>
						<xsl:when test="$publType = 'cumulativeThesis'">
							<xsl:text>thesis.doctoral</xsl:text>
						</xsl:when>
						<xsl:when test="$publType = 'masterThesis'">
							<xsl:text>master</xsl:text>
						</xsl:when>
						<xsl:when test="$publType = 'habilitation'">
							<xsl:text>thesis.habilitation</xsl:text>
						</xsl:when>
						<xsl:when test="$publType = 'diplomaThesis'">
							<xsl:text>Diplom</xsl:text>
						</xsl:when>
						<xsl:when test="$publType = 'magisterThesis'">
							<xsl:text>M.A.</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>other</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</thesis:level>
				<thesis:grantor>
					<cc:universityOrInstitution>
                                                <xsl:if test="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element[@name='universityOrInstitution']/doc:element/doc:field[@name='value']='Technische Universität Hamburg'">
                                                    <xsl:attribute name="cc:GKD-Nr">1112763473</xsl:attribute>
                                                </xsl:if>
                                                <xsl:if test="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element[@name='universityOrInstitution']/doc:element/doc:field[@name='value']='Technische Universität Hamburg-Harburg'">
                                                    <xsl:attribute name="cc:GKD-Nr">2067664-5</xsl:attribute>
                                                </xsl:if>
                                                <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element[@name='universityOrInstitution']/doc:element/doc:field[@name='value']" /></cc:name>
                                                <cc:place><xsl:value-of select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element[@name='place']/doc:element/doc:field[@name='value']" /></cc:place>
                                                <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='publication']/doc:element[@name='institute']/doc:element/doc:field[@name='value']">
                                                    <cc:department>
<!-- Goettinger Institutselement
                                                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='affiliation']/doc:element[@name='institute']/doc:element/doc:field[@name='value']">
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='affiliation']/doc:element[@name='institute']/doc:element/doc:field[@name='value']"/></cc:name>
                                                        </xsl:if>
-->
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='publication']/doc:element[@name='institute']/doc:element/doc:field[@name='value']"/></cc:name>
<!--
                                                        <xsl:if test="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element/doc:field[@name='value']">
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='thesis']/doc:element[@name='grantor']/doc:element/doc:field[@name='value']"/></cc:name>
                                                        </xsl:if>
-->
                                                    </cc:department>
                                                </xsl:if>
					</cc:universityOrInstitution>
				</thesis:grantor>
			</thesis:degree>
                        </xsl:if>

			<ddb:contact ddb:contactID="{$dnbnr}" />
		   	<!-- fileSection -->
			<xsl:variable name="handle"><xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']" /></xsl:variable>
			<xsl:variable name="fileNumber">
				<xsl:for-each select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']">
					<xsl:if test="(doc:field/text() = 'ORIGINAL')">
						<xsl:value-of select="count(doc:element[@name='bitstreams']/doc:element[@name='bitstream'])"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<ddb:fileNumber><xsl:value-of select="$fileNumber"/></ddb:fileNumber>
                        <!-- 44. File properties -->
                        <xsl:for-each 
                            select="/doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:field[@name='name' and text()='ORIGINAL']/../doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
                            <ddb:fileProperties>
                                <xsl:attribute name="ddb:fileName">
                                    <xsl:value-of select="./doc:field[@name='name']"/>
                                </xsl:attribute>
                                <xsl:attribute name="ddb:fileSize">
                                    <xsl:value-of select="./doc:field[@name='size']"/>
                                </xsl:attribute>
                            </ddb:fileProperties>
                        </xsl:for-each>
                        <!-- ddb:transfer - normal bitstream link if 1 or special retrieve link > 1 -->
                        <!-- the "url" in xoai assumes that dspace is deployed as root application -->
                        <xsl:variable name="bundleName">
                            <!-- If there is more than only one file, get the archive, otherwise the original bundle -->
                            <xsl:choose>
                                <xsl:when test="$fileNumber > '1'">ARCHIVE</xsl:when>
                                <xsl:otherwise>ORIGINAL</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:for-each select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:field[@name='name' and text()=$bundleName]/../doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
                            <!-- 45. Checksum -->
                            <ddb:checksum>
                                <xsl:attribute name="ddb:type">
                                    <xsl:value-of select="./doc:field[@name='checksumAlgorithm']"/>
                                </xsl:attribute>
                                <xsl:value-of select="./doc:field[@name='checksum']"/>
                            </ddb:checksum>

                            <!-- 46. Transfer-URL -->
                            <ddb:transfer ddb:type="dcterms:URI">
                                <xsl:value-of select="str:replaceAll(str:new(./doc:field[@name='url']/text()), '\+', '\%20')"/>
                            </ddb:transfer>
                        </xsl:for-each>
			<ddb:identifier ddb:type="URL"><xsl:value-of select="$handle"/></ddb:identifier>

                        <!-- select all rights -->
                        <!-- RULES:
                        mixedopen, open = free
                        embargo_ = blocked
                        restricted, embargo_restricted_, mixedrestricted, reserved = domain -->

                        <!-- grantfulltext variable is used to checking if there is an embargo period -->
                        <xsl:variable name="grantfulltext" select="doc:metadata/doc:element[@name='item']/doc:element[@name='grantfulltext']//doc:field[@name='value']"/>
                        <xsl:variable name="rights" select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']"/>
                        <xsl:variable name="rightsuri" select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']//doc:field[@name='value']"/>
                        <xsl:variable name="embargodate"/>

			<ddb:rights>
				<xsl:attribute name="ddb:kind">
                        <xsl:if test="$grantfulltext!=''">
                            <xsl:choose>
                                <xsl:when test="contains($grantfulltext, 'open')">
                                    <xsl:text>free</xsl:text>
                                </xsl:when>
                                <xsl:when test="contains($grantfulltext, 'embargo_')">
                                    <xsl:text>blocked</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>domain</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
				</xsl:attribute>
				<xsl:if test="$grantfulltext!='' and contains($grantfulltext, 'embargo_')">
                                    <xsl:text>Embargo bis </xsl:text><xsl:value-of select="substring($grantfulltext, 15, 2)"/><xsl:text>.</xsl:text><xsl:value-of select="substring($grantfulltext, 13, 2)"/><xsl:text>.</xsl:text><xsl:value-of select="substring($grantfulltext, 9, 4)"/><xsl:text>, danach "free".</xsl:text>
				</xsl:if>
			</ddb:rights>
	  </xMetaDiss:xMetaDiss> 
	</xsl:template>

</xsl:stylesheet>

