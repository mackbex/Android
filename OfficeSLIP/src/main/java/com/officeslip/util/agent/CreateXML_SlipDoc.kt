package com.officeslip.util.agent

import android.app.Activity
import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.officeslip.*
import com.officeslip.data.model.Slip
import com.officeslip.SysInfo
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import org.jdom2.Document

import org.xml.sax.InputSource
import org.jdom2.input.SAXBuilder
import org.jdom2.Element
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.io.*


class CreateXML_SlipDoc {

    private var m_C = Common()

    //Create DocData
    fun getElement_DocData(context: Context):Element? {
        var elDocData:Element? = null

        context.assets.open("common/SLIPDOC.xml")?.run {

            elDocData = Element("DocData")
            val builder = SAXBuilder()
            val document = builder.build(InputSource(InputStreamReader(this, "UTF-8")))
            val rootNode = document.rootElement
            val listDocData = rootNode.getChild("DocData").getChildren("Field")

            for(element in listDocData)
            {
                //Parse from SLIPDOC Form
                var elItem = Element(element.getAttributeValue("NAME"))
                elItem.setAttribute("TITLE",element.getAttributeValue("TITLE"))
                //If default value does exist..
                element.getAttributeValue("VALUE")?.apply {
                    elItem.text = this
                }

                elDocData?.addContent(elItem)
            }
        }

        return elDocData
    }

    //Create File node
    fun getElement_FileList(strDocIRN:String, nPage:Int ):Element?
	{
        var elFileList = Element("FileList")
        elFileList.setAttribute("TYPE","JPG")

        var elFile = Element("File")
        elFile.setAttribute("Num","0");
        elFile.setAttribute("Page", ""+nPage)
        elFile.setAttribute("Name",strDocIRN+".TIF")
        elFile.setAttribute("update_time","")

        elFileList.addContent(elFile)

		return elFileList
	}


    //Create DocList node
    fun getElement_DocListItem(elDocList:Element?, slipList: ArrayList<Slip>, strSDocNo: String, isSDocOne:Boolean):Element?
    {

        var nPage = 1

        if(isSDocOne)
        {
//            var  strDocIRNLine:String? = null
//            var nIdx = 0
//            for (slip in slipList)
//            {
//                var strDocIRN = slip.docIrn
//
//                strDocIRNLine += "$strDocIRN.J2K"
//
//                if(nIdx < slip.size -1)
//                {
//                    strDocIRN += ";"
//                }
//            }
//
//            var elDoc = Element("DOC")
//
//            elDoc.setAttribute("cabinet",m_C.getDate("yyyyMMdd"))
//            elDoc.setAttribute("folder", AGENT_FOLDER)
//            elDoc.setAttribute("doc_irn",arObjImages.get(0).asJsonObject.get("DocIRN").asString)
//            elDoc.setAttribute("sdoc_no",strSDocNo)
//            elDoc.setAttribute("slip_no",String.format("%04d",nPage))
//            elDoc.setAttribute("Comment", "SLIP")
//            elDoc.setAttribute("File",strDocIRNLine)
//            elDoc.setAttribute("file_cnt","1")
//            elDoc.setAttribute("doc_status","1")
//            elDoc.setAttribute("bCompress","1")
//            elDoc.setAttribute("slip_flag","0")
//            elDoc.setAttribute("file_size", m_C.getSlipTotalSize(arObjImages).toString())
//            elDoc.setAttribute("update_time",m_C.getDate("yyMMddhhmmsss"))
//
//            elDocList?.addContent(elDoc)
        }
        else {
            for (slip in slipList) {
//                var obj = obj.asJsonObject

                var elDoc = Element("DOC")

                elDoc.setAttribute("cabinet", m_C.getDate("yyyyMMdd"))
                elDoc.setAttribute("Folder", AGENT_FOLDER)
                elDoc.setAttribute("doc_irn", slip.docIrn)
                elDoc.setAttribute("sdoc_no", strSDocNo)
                elDoc.setAttribute("slip_no", String.format("%04d",nPage))
                elDoc.setAttribute("Comment", "SLIP")
                elDoc.setAttribute("File", slip.docIrn + ".J2K")
                elDoc.setAttribute("file_cnt", "1")
                elDoc.setAttribute("doc_status", "1")
                elDoc.setAttribute("bCompress", "1")
                elDoc.setAttribute("slip_flag", "0")
                elDoc.setAttribute("file_size", "${slip.fileSize}")
                elDoc.setAttribute("update_time", m_C.getDate("yyMMddhhmmsss"))

                elDocList?.addContent(elDoc)
                nPage++
            }

        }


        elDocList
        return elDocList
    }

    //Create DocList node
    fun getElement_DocListItemForCopy(elDocList:Element?, slipList: ArrayList<Slip>, strSDocNo: String, isSDocOne:Boolean):Element?
    {
        if(slipList == null) return null

        var nPage = 1

        if(isSDocOne)
        {
//            var  strDocIRNLine:String? = null
//            var nIdx = 0
//            for (obj in slipList)
//            {
//                var obj = obj.asJsonObject
//                var strDocIRN = obj.get("DOC_IRN").asString
//
//                strDocIRNLine += "$strDocIRN.J2K"
//
//                if(nIdx < obj.size() -1)
//                {
//                    strDocIRN += ";"
//                }
//            }
//
//            var elDoc = Element("DOC")
//
//            elDoc.setAttribute("cabinet",m_C.getDate("yyyyMMdd"))
//            elDoc.setAttribute("folder", arObjImages.get(0).asJsonObject.get("FOLDER").asString)
//            elDoc.setAttribute("doc_irn",arObjImages.get(0).asJsonObject.get("DOC_IRN").asString)
//            elDoc.setAttribute("sdoc_no",strSDocNo)
//            elDoc.setAttribute("slip_no",String.format("%04d",nPage))
//            elDoc.setAttribute("Comment", "SLIP")
//            elDoc.setAttribute("File",strDocIRNLine)
//            elDoc.setAttribute("file_cnt","1")
//            elDoc.setAttribute("doc_status","1")
//            elDoc.setAttribute("bCompress","1")
//            elDoc.setAttribute("slip_flag","0")
//            elDoc.setAttribute("file_size", getSlipTotalSize(slip))
//            elDoc.setAttribute("update_time",arObjImages.get(0).asJsonObject.get("UPDATE_TIME").asString)
//
//            elDocList?.addContent(elDoc)
        }
        else {
            for (i in 0 until slipList.size) {
                val slip = slipList[i]

                var elDoc = Element("DOC")

                elDoc.setAttribute("cabinet", m_C.getDate("yyyyMMdd"))
                elDoc.setAttribute("Folder", AGENT_FOLDER)
                elDoc.setAttribute("doc_irn", slip.docIrn)
                elDoc.setAttribute("sdoc_no", strSDocNo)
                elDoc.setAttribute("slip_no", String.format("%04d",i + 1))
                elDoc.setAttribute("Comment", "SLIP")
                elDoc.setAttribute("File", slip.docIrn + ".J2K")
                elDoc.setAttribute("file_cnt", "1")
                elDoc.setAttribute("doc_status", "1")
                elDoc.setAttribute("bCompress", "1")
                elDoc.setAttribute("slip_flag", "0")
                elDoc.setAttribute("file_size", "${slip.fileSize}")
                elDoc.setAttribute("update_time", m_C.getDate("yyyyMMddhhmmsss"))

                elDocList?.addContent(elDoc)
                nPage++
            }

        }

        return elDocList
    }


    //Fill DocData items
    fun setDocData(elDocData:Element?,docIrn:String, documentInfo:com.officeslip.data.model.Document, slipList:ArrayList<Slip>,  isSDocOne:Boolean):Element? {

        if(elDocData == null) return null

        var listElItem = elDocData.children
        for(el in listElItem)
        {
            var strElName = el.name.toUpperCase()
            when(strElName)
            {
                "CABINET" -> {
                    el.text = m_C.getDate("yyyyMMdd")
                }
                "FOLDER" -> {
                    el.text = AGENT_FOLDER
                }
                "DOC_IRN" -> {
                    el.text = docIrn
                }
                "SDOC_NO" -> {
                    el.text = documentInfo.sdocNo
                }
                "SDOCNO_DOC"-> {
                    el.text = documentInfo.jdocNo
                }
                "SDOC_TYPE" -> {
                    el.text = documentInfo.slipKindNo
                }
                "SDOC_ONE" -> {
                    el.text = if(isSDocOne) "1" else "0"
                }
                "SDOC_MONTH" -> {
                    el.text = m_C.getDate("yyyyMM")
                }
                "SDOC_NAME" -> {
                    el.text = documentInfo.sdocName
                }
                "CO_NO" -> {
                    el.text = if(SysInfo.IS_SCHEME_CALL) { SysInfo.schemeUserInfo[corpNo].asString } else { SysInfo.userInfo[corpNo].asString  }
                }
                "PART_NO" -> {
                    el.text = if(SysInfo.IS_SCHEME_CALL) { SysInfo.schemeUserInfo[partNo].asString } else { SysInfo.userInfo[partNo].asString  }
                }
                "HG_DATE" -> {
                    el.text = m_C.getDate("yyyyMMdd")
                }
                "SLIP_CNT" -> {
                    el.text = if(isSDocOne) "1" else "${slipList.size}"
                }
                "FILE_CNT" -> {
                    el.text = "${slipList.size}"
                }
                "FILE_SIZE" -> {
                    el.text = "${getSlipTotalSize(slipList)}"
                }
                "REG_USER" -> {
                    el.text = if(SysInfo.IS_SCHEME_CALL) { SysInfo.schemeUserInfo[userId].asString } else { SysInfo.userInfo[userId].asString  }
                }
                "REG_USERNM" -> {
                    el.text = if(SysInfo.IS_SCHEME_CALL) { SysInfo.schemeUserInfo[userNm].asString } else { SysInfo.userInfo[userNm].asString  }
                }
                "REG_TIME" -> {
                    el.text = m_C.getDate("yyyyMMddHHmmsss")
                }
            }
        }

        return elDocData
    }

    //Fill DocData items
    fun setDocDataForCopy(elDocData:Element?, objSlipDocInfo: JsonObject, objUserInfo:JsonObject, strDocIRN:String, strSDocNo:String):Element? {

        if(elDocData == null) return null

      ///  {"USER_ID":"oater1","USER_NAME":"김한결","PART_CD":"1201016","CO_CD":"01","PART_NM":"솔루션 개발팀","CO_NAME":"(주)우남소프트","POSITION":"0"}
        var strUserID= objUserInfo.get("USER_ID").asString
        var strUserName= objUserInfo.get("USER_NAME").asString
        var strPartCD= objUserInfo.get("PART_CD").asString
        var strCoCD= objUserInfo.get("CO_CD").asString


        var listElItem = elDocData.children
        for(el in listElItem)
        {
            var strElName = el.name.toUpperCase()
            when(strElName)
            {
                "CABINET" -> {
                    el.text = objSlipDocInfo.get("CABINET")?.asString
                }
                "FOLDER" -> {
                    el.text = AGENT_FOLDER
                }
                "DOC_IRN" -> {
                    el.text = strDocIRN
                }
                "SDOC_NO" -> {
                    el.text = strSDocNo
                }
                "SDOCNO_DOC"-> {
                    objSlipDocInfo.get("JDOC_NO")?.apply {
                        el.text = this.asString
                    }
                }
                "SDOC_TYPE" -> {
                    el.text = objSlipDocInfo.get("SDOC_TYPE")?.asString
                }
                "SDOC_ONE" -> {
                    el.text = objSlipDocInfo.get("SDOC_ONE")?.asString
                }
                "SDOC_MONTH" -> {
                    el.text = objSlipDocInfo.get("SDOC_MONTH")?.asString
                }
                "SDOC_NAME" -> {
                    el.text = objSlipDocInfo.get("SDOC_NAME")?.asString
                }
                "CO_NO" -> {
                    el.text = strCoCD
                }
                "PART_NO" -> {
                    el.text = strPartCD
                }
                "HG_DATE" -> {
                    el.text = objSlipDocInfo.get("HG_DATE")?.asString
                }
                "SLIP_CNT" -> {
                    el.text = objSlipDocInfo.get("SLIP_CNT")?.asString
                }
                "FILE_CNT" -> {
                    el.text = objSlipDocInfo.get("FILE_CNT")?.asString
                }
                "FILE_SIZE" -> {
                    el.text = objSlipDocInfo.get("FILE_SIZE")?.asString
                }
                "REG_USER" -> {
                    el.text = strUserID
                }
                "REG_USERNM" -> {
                    el.text = strUserName
                }
                "REG_TIME" -> {
                    el.text = objSlipDocInfo.get("REG_TIME")?.asString
                }
            }
        }

        return elDocData
    }



    fun getElement_PageList(slipList: ArrayList<Slip>, isSDocOne: Boolean):ArrayList<Element>? {

        var alRes = ArrayList<Element>()
//        val strSubsdoc_no = "증빙"
        var nIdx = 1

        for(i in 0 until slipList.size)
        {
            var elPage:Element = Element("Page")
            var slip = slipList[i]
            val strSubsdoc_no = slip.sdocNo
            if(isSDocOne)
            {
                elPage.setAttribute("Num","1-"+nIdx)
                elPage.setAttribute("Comment",strSubsdoc_no + "-001-" + String.format("%03d",nIdx))
                elPage.setAttribute("ID",strSubsdoc_no+"+"+"0001")
                elPage.setAttribute("LINE",strSubsdoc_no+"+"+String.format("%04d",nIdx))
                elPage.setAttribute("Thumb",strSubsdoc_no + "-" + "001")

            }
            else
            {
                elPage.setAttribute("Num",(nIdx).toString()+"-1")
                elPage.setAttribute("Comment",strSubsdoc_no + "-" + String.format("%03d",nIdx) + "-001")
                elPage.setAttribute("ID",strSubsdoc_no+"+"+String.format("%04d",nIdx))
                elPage.setAttribute("LINE",strSubsdoc_no+"+"+String.format("%04d",nIdx))
                elPage.setAttribute("Thumb",strSubsdoc_no + "-" +  String.format("%03d",nIdx))
            }

            elPage.setAttribute("Title","첨부문서")
            elPage.setAttribute("PageFlag","C")
            elPage.setAttribute("Rect","0,0,"+m_C.pixel2pt(slip.width)+","+m_C.pixel2pt(slip.height))

		    var elFill = Element("Fill")
            elFill.setAttribute("Type","FILL")
            elFill.setAttribute("Style","Default")

            var elColor = Element("Color")
            elColor.setText("255,255,255")

            var elFill2 = Element("Fill")
            elFill2.setText("100");

            var elImage = Element("Image")
            elImage.setAttribute("Type","IMAGEBOX")
            elImage.setAttribute("Flag","Center|VCenter")
            elImage.setAttribute("DPI","200")

            if(isSDocOne)	elImage.setAttribute("File","\$DOC(1,"+nIdx+")")
            else			elImage.setAttribute("File","\$DOC("+nIdx+",1)")

            elImage.setText("")

            elFill.addContent(elColor)
            elFill.addContent(elFill2)
            elFill.addContent(elImage)

            elPage.setContent(elFill)

            alRes.add(elPage)
            nIdx++
        }

        return alRes
    }

//    fun getElement_PageListForCopy(arObjImages: JsonArray, isSDocOne: Boolean):ArrayList<Element>? {
//
//        if(arObjImages == null) return null
//
//        var alRes = ArrayList<Element>()
//        val strSubsdoc_no = "증빙"
//        var nIdx = 1
//
//        for(obj in arObjImages)
//        {
//            var obj         = obj.asJsonObject
//            var strRect         = obj.get("SLIP_RECT").asString
//            var strShape        = obj.get("SLIP_PAGE").asString
//
//            var elPage:Element = Element("Page")
//
//            if(isSDocOne)
//            {
//                elPage.setAttribute("Num","1-"+nIdx)
//                elPage.setAttribute("Comment",strSubsdoc_no + "-001-" + m_C.getSlipNo(3, nIdx))
//                elPage.setAttribute("ID",strSubsdoc_no+"+"+"0001")
//                elPage.setAttribute("LINE",strSubsdoc_no+"+"+m_C.getSlipNo(4, nIdx))
//                elPage.setAttribute("Thumb",strSubsdoc_no + "-" + "001")
//
//            }
//            else
//            {
//                elPage.setAttribute("Num",(nIdx).toString()+"-1")
//                elPage.setAttribute("Comment",strSubsdoc_no + "-" + m_C.getSlipNo(3, nIdx) + "-001")
//                elPage.setAttribute("ID",strSubsdoc_no+"+"+m_C.getSlipNo(4, nIdx))
//                elPage.setAttribute("LINE",strSubsdoc_no+"+"+m_C.getSlipNo(4, nIdx))
//                elPage.setAttribute("Thumb",strSubsdoc_no + "-" +  m_C.getSlipNo(3, nIdx))
//            }
//
//            elPage.setAttribute("Title","첨부문서")
//            elPage.setAttribute("PageFlag","C")
//
//            if(m_C.isBlank(strRect))
//            {
//                strRect = "0,0,5184,3240"
//            }
//            elPage.setAttribute("Rect",strRect)
//
//            var elFill = Element("Fill")
//            elFill.setAttribute("Type","FILL")
//            elFill.setAttribute("Style","Default")
//
//            var elColor = Element("Color")
//            elColor.setText("255,255,255")
//
//            var elFill2 = Element("Fill")
//            elFill2.setText("100")
//
//            var elImage = Element("Image")
//            elImage.setAttribute("Type","IMAGEBOX")
//            elImage.setAttribute("Flag","Center|VCenter")
//            elImage.setAttribute("DPI","200")
//
//            if(isSDocOne)	elImage.setAttribute("File","\$DOC(1,"+nIdx+")")
//            else			elImage.setAttribute("File","\$DOC("+nIdx+",1)")
//
//            elImage.setText("")
//
//            elFill.addContent(elColor)
//            elFill.addContent(elFill2)
//            elFill.addContent(elImage)
//
//            elPage.setContent(elFill)
//
//
//            if(!m_C.isBlank(strShape))
//            {
//               // elPage.addContent(strShape)
//                strShape = strShape.replace("[", "<")
//                strShape = strShape.replace("]", ">")
//            }
//
//            try {
//                var sbShapeXML = StringBuilder().apply{
//                    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
//                    append("<Document>")
//                    append(strShape)
//                    append("</Document>")
//                }
//                val builder = SAXBuilder()
//                val stream = ByteArrayInputStream(sbShapeXML.toString().toByteArray(charset("UTF-8")))
//                val document = builder.build(stream)
//                var elItems = document.rootElement.children
//
//                for(elItem in elItems)
//                {
//                    elPage.addContent(elItem.clone())
//                }
//            } catch (e: Exception) {
//                Logger.error("getElement_PageListForCopy", e)
//            }
//
//            alRes.add(elPage)
//            nIdx++
//        }
//
//        return alRes
//    }

    /**
     * Write xml to file.
     * @return XML Path or null
     */
    fun write(elDocument:Element, strDocIRN:String):String?
    {
        var strRes:String? = null

        var strSavePath = UPLOAD_PATH + File.separator


        var document = Document(elDocument)

        var path = File(strSavePath)

        if(!path.exists())
        {
            path.mkdirs()
        }

        var file = File(path.absolutePath, "$strDocIRN.XML")

        FileOutputStream(file).use {

            var format = Format.getPrettyFormat()
            format.encoding = SysInfo.CHARSET
            format.indent = "  "
            format.lineSeparator = "\r\n"
            var serializer = XMLOutputter(format)
            serializer.output(document, it)


            it.flush()

            strRes = strSavePath + "$strDocIRN.XML"
        }

		return strRes
    }

    private fun getSlipTotalSize(slipList:ArrayList<Slip>):Long {
        var size = 0L
        for(slip in slipList)  {
            size += slip.fileSize
        }

        return size
    }
//
//public Element makePageTag(String strDocIrn, String strSdocNo, String strTitle, String strFlag, int nPage, int nWidth, int nHeight )
//	{
//		String strJ 							= Integer.toString(nPage);
//		String strSubsdoc_no 			=  "증빙";//strSdocNo.substring(strSdocNo.length()-6, strSdocNo.length());
//		String strSlipno3					= common.GetSlipNO(3,nPage);
//		String strSlipno4				 	= common.GetSlipNO(4,nPage);
//		String strWidHei[]	= {"400","300"};
////		String strWidHei[] = TC.getImgSize(ArrFilePath[j-1]).split(";");
//
//		Element Page = new Element("Page");
//
//		if(strFlag.equals("0"))
//		{
//			Page.setAttribute("Num",strJ+"-1");
//			Page.setAttribute("Comment",strSubsdoc_no + "-" + strSlipno3 + "-001");
//			Page.setAttribute("ID",strSdocNo+"+"+strSlipno4);
//			Page.setAttribute("LINE",strSdocNo+"+"+strSlipno4);
//			Page.setAttribute("Thumb",strSubsdoc_no + "-" +  strSlipno3);
//		}
//		else
//		{
//			Page.setAttribute("Num","1-"+strJ);
//			Page.setAttribute("Comment",strSubsdoc_no + "-001-" + strSlipno3);
//			Page.setAttribute("ID",strSdocNo+"+"+"0001");
//			Page.setAttribute("LINE",strSdocNo+"+"+strSlipno4);
//			Page.setAttribute("Thumb",strSubsdoc_no + "-" + "001");
//		}
//
//		Page.setAttribute("Title","첨부문서");
//		Page.setAttribute("PageFlag","C");
//		Page.setAttribute("Rect","0,0,"+nWidth+","+nHeight);
//
//		Element Fill = new Element("Fill");
//		Fill.setAttribute("Type","FILL");
//		Fill.setAttribute("Style","Default");
//
//		Element color = new Element("Color");
//		color.setText("255,255,255");
//
//		Element Fill2 = new Element("Fill");
//		Fill2.setText("100");
//
//		Element Image = new Element("Image");
//		Image.setAttribute("Type","IMAGEBOX");
//		Image.setAttribute("Flag","Center|VCenter");
//		Image.setAttribute("DPI","200");
//
//		if(strFlag.equals("0"))			Image.setAttribute("File","$DOC("+nPage+",1)");
//		else									Image.setAttribute("File","$DOC(1,"+nPage+")");
//
//		Image.setText("");
//
//		Fill.addContent(color);
//		Fill.addContent(Fill2);
//		Fill.addContent(Image);
//
//		Page.setContent(Fill);
//
//		return Page;
//	}

    /*public String MakeXml(Hashtable<String,EntryXml> slipForm, String slipFlag, String strUser_id, String strSdoc_type, String strSdoc_name,
    String strCo_cd, String strPart_cd, String strDoc_irn, String strSdoc_no, String strsdocno_doc, String strSlipCnt,
    String strFileCnt, String strFileSize, String[] ArrFilePath)
    {

        int nfile_cnt = Integer.parseInt(strFileCnt);
        String retstrSliptDoc_irn = "";
        //Document 태그 생성
        Element Document = new Element("Document");
        Document.setAttribute("Type","DOCFILE");
        Element DocData = new Element("DocData");

        //DocData 태그 생성
        Set<String> keys = slipForm.keySet();
        for(String key: keys)
        {
            Element element = new Element(key);
            element.setAttribute("Title",slipForm.get(key).getTitle());
            element.setText(slipForm.get(key).getValue());
            DocData.addContent(element);
        }

        //DocData 태그를 Document 태그의 차일드로
        Document.addContent(DocData);

        Element DocList = new Element("DocList");

        DocList.setAttribute("Load","1");
        DocList.setAttribute("Table","img_slip_t");
        DocList.setAttribute("Field","cabinet;folder;doc_irn;sdoc_no;slip_no;slip_cd;file_cnt;file_size;update_time");

        if(slipFlag.equals("E"))
        {
            for(int i=1;i<=nfile_cnt;i++)
            {
                String strSliptDoc_irn    = TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);
                String strSlipno			= TC.GetSlipNO(4,i);
                String strSlipSize 			= TC.GetFileSize(ArrFilePath[i-1]);

                Element DOC = new Element("DOC");
                DOC.setAttribute("cabinet",TC.getToday());
                DOC.setAttribute("Folder",folder);
                DOC.setAttribute("doc_irn",strSliptDoc_irn);
                DOC.setAttribute("sdoc_no",strSdoc_no);
                DOC.setAttribute("slip_no",strSlipno);
                DOC.setAttribute("slip_cd","1");
                DOC.setAttribute("File",strSliptDoc_irn+".J2K");
                DOC.setAttribute("file_cnt","1");
                DOC.setAttribute("doc_status","1");
                DOC.setAttribute("bCompress","1");
                DOC.setAttribute("file_size",strSlipSize);
                DOC.setAttribute("update_time",TC.GetCurtime());
                DOC.setAttribute("Comment",folder);

                DocList.addContent(DOC);
                retstrSliptDoc_irn = retstrSliptDoc_irn + ";" + strSliptDoc_irn;
            }
        }
        else
        {
            String arrSliptDoc_irn[]  	= new String[nfile_cnt+1];
            String arrFileName 			= "";

            for(int i=1;i<=nfile_cnt;i++)
            {
                arrSliptDoc_irn[i]  	= TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);

                arrFileName			 	= arrFileName	 +  arrSliptDoc_irn[i] + ".J2K;";
                retstrSliptDoc_irn 	= retstrSliptDoc_irn + ";" + arrSliptDoc_irn[i];
            }

            String strSlipSize 			 = TC.GetFileTotalSize(ArrFilePath);

            Element DOC = new Element("DOC");
            DOC.setAttribute("cabinet",TC.getToday());
            DOC.setAttribute("Folder",folder);
            DOC.setAttribute("doc_irn",arrSliptDoc_irn[1]);
            DOC.setAttribute("sdoc_no",strSdoc_no);
            DOC.setAttribute("slip_no","0001");
            DOC.setAttribute("slip_cd","1");
            DOC.setAttribute("File",arrFileName);
            DOC.setAttribute("file_cnt",Integer.toString(nfile_cnt));
            DOC.setAttribute("doc_status","1");
            DOC.setAttribute("bCompress","1");
            DOC.setAttribute("file_size",strSlipSize);
            DOC.setAttribute("update_time",TC.GetCurtime());
            DOC.setAttribute("Comment",folder);

            DocList.addContent(DOC);
        }

        Document.addContent(DocList);

        Element FileList = new Element("FileList");
        FileList.setAttribute("TYPE","JPG");

        Element File = new Element("File");
        File.setAttribute("Num","0");
        File.setAttribute("Page",strFileCnt);
        File.setAttribute("Name",strDoc_irn+".TIF");
        File.setAttribute("update_time","");

        FileList.addContent(File);
        Document.addContent(FileList);

        for(int j=1;j<=nfile_cnt;j++)
        {
            String strJ 						= Integer.toString(j);
            //String strSubsdoc_no 	=  strSdoc_no.substring(strSdoc_no.length()-6, strSdoc_no.length());
            String strSubsdoc_no		= strSdoc_type;
            String strSlipno3				 = TC.GetSlipNO(3,j);
            String strSlipno4				 = TC.GetSlipNO(4,j);

            String strWidHei[] = TC.getImgSize(ArrFilePath[j-1]).split(";");

            Element Page = new Element("Page");

            if(slipFlag.equals("E"))
            {
                Page.setAttribute("Num",strJ+"-1");
                Page.setAttribute("Comment",strSubsdoc_no + "-" + strSlipno3 + "-001");
                Page.setAttribute("ID",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("LINE",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("Thumb",strSubsdoc_no + "-" +  strSlipno3);
            }
            else
            {
                Page.setAttribute("Num","1-"+strJ);
                Page.setAttribute("Comment",strSubsdoc_no + "-001-" + strSlipno3);
                Page.setAttribute("ID",strSdoc_no+"+"+"0001");
                Page.setAttribute("LINE",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("Thumb",strSubsdoc_no + "-" + "001");
            }

            Page.setAttribute("Title",attachedTitle);
            Page.setAttribute("PageFlag","C");
            Page.setAttribute("Rect","0,0,"+Integer.parseInt(strWidHei[0])*3+","+Integer.parseInt(strWidHei[1])*3);

            Element Fill = new Element("Fill");
            Fill.setAttribute("Type","FILL");
            Fill.setAttribute("Style","Default");

            Element color = new Element("Color");
            color.setText("255,255,255");

            Element Fill2 = new Element("Fill");
            Fill2.setText("100");

            Element Image = new Element("Image");
            Image.setAttribute("Type","IMAGEBOX");
            Image.setAttribute("Flag","Center|VCenter");
            Image.setAttribute("DPI","200");

            if(slipFlag.equals("E"))			Image.setAttribute("File","$DOC("+j+",1)");
            else									Image.setAttribute("File","$DOC(1, "+j+")");

            Image.setText("");

            Fill.addContent(color);
            Fill.addContent(Fill2);
            Fill.addContent(Image);

            Page.setContent(Fill);
            Document.addContent(Page);
        }

        try
        {
            Document doc 	= new Document(Document);
            String url 			= imgPath + strDoc_irn+".xml";
            File dir 				= new File(imgPath);

            if(!dir.exists())
            {
                dir.mkdir();
            }

            FileOutputStream fos = new FileOutputStream(url);

            XMLOutputter serializer = new XMLOutputter();

            Format fm = serializer.getFormat();

            fm.setEncoding(init.GetCharSet().toLowerCase());
            fm.setIndent("  ");

            fm.setLineSeparator("\r\n");

            serializer.setFormat(fm);

            serializer.output(doc, fos);

            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            return "";
        }
        finally
        {

        }
        return retstrSliptDoc_irn;
    }

    public String MakeXml(Hashtable<String,EntryXml> slipForm, String slipFlag, String strUser_id, String strSdoc_type, String strSdoc_name,
    String strCo_cd, String strPart_cd, String strDoc_irn, String strSdoc_no, String strsdocno_doc, String strSlipCnt,
    String strFileCnt, String strFileSize, ArrayList<EntryImage> list, Common C)
    {

        int nfile_cnt = Integer.parseInt(strFileCnt);
        String retstrSliptDoc_irn = "";
        //Document 태그 생성
        Element Document = new Element("Document");
        Document.setAttribute("Type","DOCFILE");
        Element DocData = new Element("DocData");

        //DocData 태그 생성
        Set<String> keys = slipForm.keySet();
        for(String key: keys)
        {
            Element element = new Element(key);
            element.setAttribute("Title",slipForm.get(key).getTitle());
            element.setText(slipForm.get(key).getValue());
            DocData.addContent(element);
        }

        //DocData 태그를 Document 태그의 차일드로
        Document.addContent(DocData);

        Element DocList = new Element("DocList");

        DocList.setAttribute("Load","1");
        DocList.setAttribute("Table","img_slip_t");
        DocList.setAttribute("Field","cabinet;folder;doc_irn;sdoc_no;slip_no;slip_cd;file_cnt;file_size;update_time");

        String strLastDocIRN 		= null;

        if(slipFlag.equals("E"))
        {
            for(int i=1;i<=nfile_cnt;i++)
            {
                String strSliptDoc_irn    = TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);
                try { Thread.sleep(10);	} catch (InterruptedException e) {}
                if(!C.isBlank(strLastDocIRN))
                {
                    while(true)
                    {
                        if(strLastDocIRN.equalsIgnoreCase(strSliptDoc_irn))
                        {
                            strSliptDoc_irn = TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                String strSlipno			= TC.GetSlipNO(4,i);
                String strFilePath			= C.GetSlipPath() + File.separator + list.get(i-1).getFileName();
                String strSlipSize 			= TC.GetFileSize(strFilePath);

                Element DOC = new Element("DOC");
                DOC.setAttribute("cabinet",TC.getToday());
                DOC.setAttribute("Folder",folder);
                DOC.setAttribute("doc_irn",strSliptDoc_irn);
                DOC.setAttribute("sdoc_no",strSdoc_no);
                DOC.setAttribute("slip_no",strSlipno);
                DOC.setAttribute("slip_cd","1");
                DOC.setAttribute("File",strSliptDoc_irn+".J2K");
                DOC.setAttribute("file_cnt","1");
                DOC.setAttribute("doc_status","1");
                DOC.setAttribute("bCompress","1");
                DOC.setAttribute("file_size",strSlipSize);
                DOC.setAttribute("update_time",TC.GetCurtime());
                DOC.setAttribute("Comment",folder);

                DocList.addContent(DOC);
                retstrSliptDoc_irn = retstrSliptDoc_irn + ";" + strSliptDoc_irn;

                strLastDocIRN = strSliptDoc_irn;
            }
        }
        else
        {
            String arrSliptDoc_irn[]  	= new String[nfile_cnt+1];
            String arrFileName 			= "";

            for(int i=1;i<=nfile_cnt;i++)
            {
                arrSliptDoc_irn[i]  	= TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);

                if(!C.isBlank(strLastDocIRN))
                {
                    while(true)
                    {
                        if(strLastDocIRN.equalsIgnoreCase(arrSliptDoc_irn[i]))
                        {
                            arrSliptDoc_irn[i] = TC.GetCopyIRN(userID, strServerIp, nServerPort, strServerKey);
                        }
                        else
                        {
                            break;
                        }
                    }
                }

                arrFileName			 	= arrFileName	 +  arrSliptDoc_irn[i] + ".J2K;";
                retstrSliptDoc_irn 	= retstrSliptDoc_irn + ";" + arrSliptDoc_irn[i];
                strLastDocIRN 			= arrSliptDoc_irn[i];
            }

            String strSlipSize 			 = TC.GetFileTotalSize(list, C);

            Element DOC = new Element("DOC");
            DOC.setAttribute("cabinet",TC.getToday());
            DOC.setAttribute("Folder",folder);
            DOC.setAttribute("doc_irn",arrSliptDoc_irn[1]);
            DOC.setAttribute("sdoc_no",strSdoc_no);
            DOC.setAttribute("slip_no","0001");
            DOC.setAttribute("slip_cd","1");
            DOC.setAttribute("File",arrFileName);
            DOC.setAttribute("file_cnt",Integer.toString(nfile_cnt));
            DOC.setAttribute("doc_status","1");
            DOC.setAttribute("bCompress","1");
            DOC.setAttribute("file_size",strSlipSize);
            DOC.setAttribute("update_time",TC.GetCurtime());
            DOC.setAttribute("Comment",folder);

            DocList.addContent(DOC);
        }

        Document.addContent(DocList);

        Element FileList = new Element("FileList");
        FileList.setAttribute("TYPE","JPG");

        Element elFile = new Element("File");
        elFile.setAttribute("Num","0");
        elFile.setAttribute("Page",strFileCnt);
        elFile.setAttribute("Name",strDoc_irn+".TIF");
        elFile.setAttribute("update_time","");

        FileList.addContent(elFile);
        Document.addContent(FileList);

        for(int j=1;j<=nfile_cnt;j++)
        {
            String strJ 						= Integer.toString(j);
            //String strSubsdoc_no 	=  strSdoc_no.substring(strSdoc_no.length()-6, strSdoc_no.length());
            String strSubsdoc_no		= strSdoc_type;
            String strSlipno3				 = TC.GetSlipNO(3,j);
            String strSlipno4				 = TC.GetSlipNO(4,j);
            String strFilePath			= C.GetSlipPath() + File.separator + list.get(j-1).getFileName();
            String strWidHei[] = TC.getImgSize(strFilePath).split(";");

            Element Page = new Element("Page");

            if(slipFlag.equals("E"))
            {
                Page.setAttribute("Num",strJ+"-1");
                Page.setAttribute("Comment",strSubsdoc_no + "-" + strSlipno3 + "-001");
                Page.setAttribute("ID",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("LINE",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("Thumb",strSubsdoc_no + "-" +  strSlipno3);
            }
            else
            {
                Page.setAttribute("Num","1-"+strJ);
                Page.setAttribute("Comment",strSubsdoc_no + "-001-" + strSlipno3);
                Page.setAttribute("ID",strSdoc_no+"+"+"0001");
                Page.setAttribute("LINE",strSdoc_no+"+"+strSlipno4);
                Page.setAttribute("Thumb",strSubsdoc_no + "-" + "001");
            }

            Page.setAttribute("Title",attachedTitle);
            Page.setAttribute("PageFlag","C");
            Page.setAttribute("Rect","0,0,"+Integer.parseInt(strWidHei[0])*3+","+Integer.parseInt(strWidHei[1])*3);

            Element Fill = new Element("Fill");
            Fill.setAttribute("Type","FILL");
            Fill.setAttribute("Style","Default");

            Element color = new Element("Color");
            color.setText("255,255,255");

            Element Fill2 = new Element("Fill");
            Fill2.setText("100");

            Element Image = new Element("Image");
            Image.setAttribute("Type","IMAGEBOX");
            Image.setAttribute("Flag","Center|VCenter");
            Image.setAttribute("DPI","200");

            if(slipFlag.equals("E"))			Image.setAttribute("File","$DOC("+j+",1)");
            else									Image.setAttribute("File","$DOC(1, "+j+")");

            Image.setText("");

            Fill.addContent(color);
            Fill.addContent(Fill2);
            Fill.addContent(Image);

            Page.setContent(Fill);
            Document.addContent(Page);
        }

        try
        {
            Document doc 	= new Document(Document);
            String url 			= C.GetSlipPath() + File.separator + strDoc_irn+".xml";
            File dir 				= new File(url);

            if(!dir.exists())
            {
                //	dir.mkdirs();
                dir.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(url);

            XMLOutputter serializer = new XMLOutputter();

            Format fm = serializer.getFormat();

            fm.setEncoding(init.GetCharSet().toLowerCase());
            fm.setIndent("  ");

            fm.setLineSeparator("\r\n");

            serializer.setFormat(fm);

            serializer.output(doc, fos);

            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            return "";
        }
        finally
        {

        }
        return retstrSliptDoc_irn;
    }*/
}