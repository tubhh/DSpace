<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://www.lyncode.com/xoai" 
    version="1.0">

        <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />

        <xsl:strip-space elements="*" />

        <xsl:template match="text()">
            <xsl:value-of select='normalize-space()'/>
        </xsl:template>

        <!-- global variables-->
        <xsl:variable name="handle-prefix">http://tubdok.tub.tuhh.de/handle/11420/</xsl:variable>
        <xsl:variable name="gkdnr"></xsl:variable>
        <xsl:variable name="dnbnr"></xsl:variable>

	<xsl:variable name="lang">
		<xsl:choose>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'deu'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'de'">
				<xsl:text>ger</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'en'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value'] = 'en_US'">
				<xsl:text>eng</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value']" />
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
		 <xMetaDiss:xMetaDiss xmlns:xMetaDiss="http://www.bsz-bw.de/xmetadissplus/1.3"
		    xmlns:dc="http://purl.org/dc/elements/1.1/" 
		    xmlns:dcterms="http://purl.org/dc/terms/" 
		    xmlns:bszterms="http://www.bsz-bw.de/xmetadissplus/1.3/terms" 
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
			xsi:schemaLocation="http://www.bsz-bw.de/xmetadissplus/1.3 http://www.bsz-bw.de/xmetadissplus/1.3/xmetadissplus.xsd">

			<!-- title data: dc.title, dc.title.alternative -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']">
				<dc:title xsi:type="ddb:titleISO639-2" lang="{$lang}">
					<xsl:value-of select="doc:element/doc:field[@name='value']" />
				</dc:title>
				<xsl:if test="doc:element[@name='alternative']">
					<dcterms:alternative xsi:type="ddb:talternativeISO639-2" ddb:type="translated">
            		        	        <xsl:attribute name="lang"><xsl:value-of select="doc:element[@name='alternative']/doc:element/doc:element/@name"/></xsl:attribute>
        	    	    			<xsl:value-of select="doc:element[@name='alternative']/doc:element/doc:field[@name='value']"/>
					</dcterms:alternative>
	        		</xsl:if>
    			</xsl:for-each>

			<!-- author data: dc.contributor.author -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
				<dc:creator xsi:type="pc:MetaPers">
					<pc:person>
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
										<xsl:value-of select="normalize-space(substring-before($tail, $prefix))" />
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
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:element/doc:field[@name='value']">
                            <xsl:choose>
                                <xsl:when test="../../@name='ddccode'">
                                    <dc:subject xsi:type="DDC-SG">
                                        <xsl:value-of select="."/>
                                    </dc:subject>
                                </xsl:when>
                                <xsl:when test="../../@name='ddc'">
                                    <dc:subject xsi:type="dcterms:DDC">
                                        <xsl:value-of select="."/>
                                    </dc:subject>
                                </xsl:when>
                                <xsl:when test="../../@name='classification'">
                                    <dc:subject xsi:type="SWD">
                                        <xsl:value-of select="."/>
                                    </dc:subject>
                                </xsl:when>
                                <xsl:otherwise>
                                    <dc:subject xsi:type="noScheme">
                                        <xsl:value-of select="."/>
                                    </dc:subject>
                                </xsl:otherwise>
                            </xsl:choose>
			</xsl:for-each>

			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
                            <dc:subject xsi:type="noScheme">
                                <xsl:value-of select="."/>
                            </dc:subject>
			</xsl:for-each>

			<!-- abstract data: dc.description -->
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
				<dcterms:abstract xsi:type="ddb:contentISO639-2">
                                        <xsl:attribute name="lang"><xsl:value-of select="../@name"/></xsl:attribute>
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
			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='advisor']/doc:element/doc:field[@name='value']">
				<dc:contributor xsi:type="pc:Contributor" countryCode="DE">
					<xsl:choose>
						<xsl:when test="contains(../../@name, 'eferee')">
							<xsl:attribute name="thesis:role">referee</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="thesis:role"><xsl:value-of select="../../@name"/></xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<pc:person>
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
									  <pc:foreName><xsl:value-of select="normalize-space($tail)"/></pc:foreName>
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

			<!-- contributors data: tuhh.contributor.referee -->
			<xsl:for-each select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='contributor']/doc:element[@name='referee']/doc:element/doc:field[@name='value']">
				<dc:contributor xsi:type="pc:Contributor" countryCode="DE">
					<xsl:attribute name="thesis:role"><xsl:value-of select="../../@name"/></xsl:attribute>
					<pc:person>
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
									  <pc:foreName><xsl:value-of select="normalize-space($tail)"/></pc:foreName>
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

                        <!-- License terms: dc.rights -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
                            <dc:rights xsi:type="dcterms:URI">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']" />
                            </dc:rights>
                        </xsl:if>

                        <!-- Schriftenreihe: dc.relation.ispartofseries -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
                            <dcterms:isPartOf>
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']" />
                            </dcterms:isPartOf>
                        </xsl:if>

			<!-- dates: dcterms.dateAccepted, dc.date.issued -->
			<xsl:if test="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='dateAccepted']/doc:element/doc:field[@name='value']">
				<dcterms:dateAccepted xsi:type="dcterms:W3CDTF">
					<xsl:value-of select="doc:metadata/doc:element[@name='dcterms']/doc:element[@name='dateAccepted']/doc:element/doc:field[@name='value']"/>
				</dcterms:dateAccepted>
			</xsl:if>
			<dcterms:issued xsi:type="dcterms:W3CDTF">
				<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']"/>
			</dcterms:issued>
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']">
                            <dcterms:created xsi:type="dcterms:W3CDTF">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value']"/>
                            </dcterms:created>
                        </xsl:if>

			<!-- publication type: dc.type or (if that is set) dc.type.thesis -->
			<dc:type xsi:type="bszterms:PublType">
				<xsl:value-of select="$publType"/>
			</dc:type>

			<!-- driver version: static content -->
			<dini:version_driver>publishedVersion</dini:version_driver>

    
			<!-- identifier: dc.identifier.urn -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='urn']/doc:element/doc:field[@name='value']">
                            <dc:identifier xsi:type="urn:nbn">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='urn']/doc:element/doc:field[@name='value']"/>
                            </dc:identifier>
                        </xsl:if>
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                            <dc:identifier xsi:type="doi">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']"/>
                            </dc:identifier>
                        </xsl:if>
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='hdl']/doc:element/doc:field[@name='value']">
                            <dc:identifier xsi:type="hdl">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='hdl']/doc:element/doc:field[@name='value']"/>
                            </dc:identifier>
                        </xsl:if>

			<!-- identifier: dc.identifier.isbn -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isbn']/doc:element/doc:field[@name='value']">
                            <dc:source xsi:type="ddb:ISBN">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isbn']/doc:element/doc:field[@name='value']"/>
                            </dc:source>
                        </xsl:if>

			<!-- identifier: dc.identifier.issn -->
                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='issn']/doc:element/doc:field[@name='value']">
                            <dc:source xsi:type="ddb:ISSN">
                                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='urn']/doc:element/doc:field[@name='value']"/>
                            </dc:source>
                        </xsl:if>
			<!-- language of publication: dc.language.iso -->
			<dc:language xsi:type="dcterms:ISO639-2">
				<xsl:value-of select="$lang"/>
			</dc:language>

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
					<cc:universityOrInstitution cc:GKD-Nr="2067664-5">
						<cc:name>Technische Universität Hamburg-Harburg</cc:name>
						<cc:place>Hamburg</cc:place>
						<cc:department>
<!-- Goettinger Institutselement
                                                        <xsl:if test="doc:metadata/doc:element[@name='dc']/doc:element[@name='affiliation']/doc:element[@name='institute']/doc:element/doc:field[@name='value']">
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='affiliation']/doc:element[@name='institute']/doc:element/doc:field[@name='value']"/></cc:name>
                                                        </xsl:if>
-->
<!-- Unspezifisches Fachbereichselement
                                                        <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='publikation']/doc:element[@name='fachbereich']/doc:element/doc:field[@name='value']">
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='publikation']/doc:element[@name='fachbereich']/doc:element/doc:field[@name='value']"/></cc:name>
                                                        </xsl:if>
-->
                                                        <xsl:if test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='institute']/doc:element[@name='german']/doc:element/doc:field[@name='value']">
							    <cc:name><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='institute']/doc:element[@name='german']/doc:element/doc:field[@name='value']"/></cc:name>
                                                        </xsl:if>
						</cc:department>
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
			<xsl:for-each select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']">
				<xsl:if test="(doc:field/text() = 'ORIGINAL')">
					<xsl:for-each select="doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
						<ddb:fileProperties>					
							<xsl:attribute name="ddb:fileName"><xsl:value-of select="doc:field[@name='name']"/></xsl:attribute>
						</ddb:fileProperties>
					</xsl:for-each>
				</xsl:if>			
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="$fileNumber = 1"> 
					<ddb:transfer ddb:type="dcterms:URI">
						<xsl:for-each select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']">
							<xsl:if test="(doc:field/text() = 'ORIGINAL')">
								<xsl:value-of select="doc:element[@name='bitstreams']/doc:element[@name='bitstream']/doc:field[@name='url']"/>
							</xsl:if>
						</xsl:for-each>
					</ddb:transfer>
				</xsl:when>
				<xsl:otherwise>		
					<xsl:variable name="baseUrl"><xsl:value-of select="substring-before(//doc:element[@name='bitstream'][1]/doc:field[@name='url']/text(), '/bitstream')" /></xsl:variable>
					<!--<ddb:transfer ddb:type="dcterms:URI"><xsl:value-of select="concat($baseUrl, '/download', substring-after($handle, $download-prefix), '-files.zip')"/></ddb:transfer>-->
				</xsl:otherwise> 
			</xsl:choose>	
			<!--<ddb:identifier ddb:type="handle"><xsl:value-of select="$handle"/></ddb:identifier>-->
                        <!-- ddb:identifier ddb:type=URL => Dieser Wert wird in Feld 7133 bzw. 4083 geschrieben -->
<!--
                        <xsl:choose>
                            <xsl:when test="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                                <ddb:identifier ddb:type="URL">https://doi.org/<xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']"/></ddb:identifier>
                            </xsl:when>
                            <xsl:otherwise>
			        <ddb:identifier ddb:type="URL"><xsl:value-of select="$handle"/></ddb:identifier>
                            </xsl:otherwise>
                        </xsl:choose>
-->
			<ddb:rights>
				<xsl:attribute name="ddb:kind">
					<xsl:choose>
					<xsl:when test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='embargo']/doc:element[@name='date']/doc:element/doc:field[@name='value']">
						<xsl:text>blocked</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>free</xsl:text>
					</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<xsl:choose>
					<xsl:when test="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='embargo']/doc:element[@name='date']/doc:element/doc:field[@name='value']">
						<xsl:text>Embargo bis </xsl:text><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='embargo']/doc:element[@name='date']/doc:element/doc:field[@name='value']"/><xsl:text>, danach "free". Grund: </xsl:text><xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='embargo']/doc:element[@name='reason']/doc:element/doc:field[@name='value']" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="doc:metadata/doc:element[@name='tuhh']/doc:element[@name='embargo']/doc:element[@name='reason']/doc:element/doc:field[@name='value']" />
					</xsl:otherwise>
				</xsl:choose>
			</ddb:rights>
	  </xMetaDiss:xMetaDiss> 
	</xsl:template>

</xsl:stylesheet>

