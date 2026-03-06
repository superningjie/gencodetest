package tdkw.opa.tdkw_opa.formplugin.model;

public class SendMessageDto {

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文
     */
    private String context;

    /**
     * 唯一流水号
     */
    private String docCode;

    /**
     * 业务层级类型
     */
    private String busType;

    /**
     * 当前状况（流程描述）
     */
    private String currentStatus;

    /**
     * pc回调地址 (统一待办)
     */
    private String pcUrl;

    /**
     * pc回调地址 (XXXX信)
     */
    private String pcYXUrl;

    /**
     * app回调地址
     */
    private String appUrl;

    /**
     * 操作类型：1、待办；2、已办；3、待阅；4、结束（统一待办）
     */
    private int optType;

    /**
     * 创建人（发起人）域账号
     */
    private String creator;

    /**
     * 创建人（发起人）名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private String createDateTime;

    /**
     * 消息接收人域账号
     */
    private String receivers;

    /**
     * 消息接收人姓名
     */
    private String receiverNames;

    /**
     * 消息接收时间
     */
    private String receiveDateTime;

    /**
     * 是否支持批量操作
     */
    private Boolean isSupportBatch;

    /**
     * 消息审批时间
     */
    private String approveDateTime;

    /**
     * XXXX信消息状态 已处理: 1 已删除: 27 已暂停: 34 已撤销: 35（XXXX信消息）
     */
    private String msgStatus;

    /**
     * 实体标识
     */
    private String entityNumber;

    /**
     * 移动端实体标识
     */
    private String mobileEntityNumber;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDocCode() {
        return docCode;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getPcUrl() {
        return pcUrl;
    }

    public void setPcUrl(String pcUrl) {
        this.pcUrl = pcUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public int getOptType() {
        return optType;
    }

    public void setOptType(int optType) {
        this.optType = optType;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiverNames() {
        return receiverNames;
    }

    public void setReceiverNames(String receiverNames) {
        this.receiverNames = receiverNames;
    }

    public String getReceiveDateTime() {
        return receiveDateTime;
    }

    public void setReceiveDateTime(String receiveDateTime) {
        this.receiveDateTime = receiveDateTime;
    }

    public Boolean getSupportBatch() {
        return isSupportBatch;
    }

    public void setSupportBatch(Boolean supportBatch) {
        isSupportBatch = supportBatch;
    }

    public String getApproveDateTime() {
        return approveDateTime;
    }

    public void setApproveDateTime(String approveDateTime) {
        this.approveDateTime = approveDateTime;
    }

    public String getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(String msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getPcYXUrl() {
        return pcYXUrl;
    }

    public void setPcYXUrl(String pcYXUrl) {
        this.pcYXUrl = pcYXUrl;
    }

    public String getEntityNumber() {
        return entityNumber;
    }

    public void setEntityNumber(String entityNumber) {
        this.entityNumber = entityNumber;
    }

    public String getMobileEntityNumber() {
        return mobileEntityNumber;
    }

    public void setMobileEntityNumber(String mobileEntityNumber) {
        this.mobileEntityNumber = mobileEntityNumber;
    }
}
