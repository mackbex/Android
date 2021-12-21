package com.officeslip


const val GET_VERSION_INFO = " SELECT ENV_VALUE FROM IMG_ENV_T WHERE ENV_NM = ? "
const val GET_USER_INFO = " EXEC ProcImg_User_Info ?, ?, ?  "
const val GET_USER_CORP = " EXEC ProcImg_Corp_User ?, ? "
const val RESET_PASSWORD = " EXEC ProcImg_User_PW_Reset ?, ? "
const val GET_USER_LIST = " EXEC ProcImg_User ?, ?, ?, ?  "

const val GET_SLIPKIND_LIST = " EXEC ProcImg_SlipKind ?, ?, ?, ?, ?  "
const val INSERT_BARCODE_SLIP = " EXEC ProcImg_Barcode ?, ? "
const val ADD_HISTORY = " EXEC ProcImg_History_Add ?, ?, ?, ?, ?, ? "
const val CHECK_THUMB_CNT_BY_DOCIRN = " Select Count(DOC_IRN) As CNT From WD_IMG_SLIP_M Where DOC_IRN In (?)"


const val SEARCH_SLIP_LIST = " ProcImg_Search ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
const val GET_SLIP_DATA = " EXEC ProcImg_Slip ?, ?, ? "
const val GET_PROPERTY = " EXEC ProcImg_SlipDOC ?, ?, ? "
const val GET_HISTORY = " EXEC ProcImg_History ?, ?, ? "


const val COPY_SLIP = " EXEC ProcImg_Copy_Slip ?, ?,'0', ?, ?, ? "
const val MOVE_SLIP = " EXEC ProcImg_Move ?, ?, ?, ?, ?, ? "


const val INSERT_SLIP = " Insert Into IMG_SLIP_T ( SLIP_IRN, CABINET, FOLDER, DOC_IRN, SDOC_NO, SLIP_NO, REG_TIME, FILE_SIZE, SLIP_TITLE, SLIP_STEP," +
        " SLIP_FLAG, SLIP_RECT, SLIP_ROTATE )" +
        " Values ( ?, getDate(), 'SLIP', ?, ?, ?, getDate(), ?, ?, ?, ?, ?, ? ) "


const val INSERT_SLIPDOC = " Insert Into IMG_SLIPDOC_T  ( CABINET, FOLDER, SDOC_NO, CORP_NO, PART_NO, REG_USER, SDOC_MONTH, SDOC_KIND, " +
        "SDOC_STEP, SDOC_FLAG, SDOC_URL, SDOC_AFTER, SDOC_PTI, SDOC_COPY, SDOC_SECU, SDOC_SYSTEM, SDOC_NAME, SDOC_DEVICE," +
        " SLIP_CNT, REG_TIME, UPDATE_TIME, JDOC_NO, JDOC_INDEX, SDOC_FOLLOW, ORG_USER) " +
        "Values (" +
        " getDate(), 'SLIPDOC', ?, ?, ?, ?, ?, ?, '0', '1', '0', '0', '10' ,'0', '1', '0', ?, 'AN', ?, getDate(), getDate(), '', '', '0', ? ) "

const val REMOVE_SLIP = " EXEC ProcImg_Slip_Del ?, ?, ?, ? "
const val REMOVE_ATTACH = " EXEC ProcImg_AddFile_Del ?, ?, ?, ? "

const val GET_QR_INFO = " EXEC ProcImg_SearchRecv ?, ?, ?, ?, ?, ?, ? "
const val CHANGE_QR_RECV_STATUS = " EXEC ProcImg_BarCode_Recv ?, ? ,?, 'AN' "

const val INSERT_BUG_REPORT = " insert into MOBILE_REPORT_T( DOC_IRN, CABINET, FOLDER, CO_NO, PART_NO, ADD_NO, ADD_NAME, ADD_FILE, REG_USER, REG_TIME,  FILE_SIZE ) " +
        " values ( ?, ?, ?, '0000', '00000000', ?, ?, ?, ?, ?, ? ) "
