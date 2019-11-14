---
name: New integration
about: New e-Commerce integration
title: Enable ECOMM_NAME integration
labels: 'enhancement'
assignees: ''
---

### Use Case:

Frontend e-commerce systems offer an excellent customer experience by providing _Subscription Management, Payment Options, Billing & Accounting, Analytics, Security & Compliance_.
ISV will be able to use NetLicensing Gateway integration with the chosen frontend e-commerce system to allow customers to acquire licenses for their products. After license(-s) acquisition, necessary assets will be created and assigned to the customer within [NetLicensing](https://netlicensing.io) system during the fulfilment process.

### Typical integration tasks:

- NetLicensing creates and delivers Customer number to be used as an identifier during the shopping process
- E-Commerce system triggers configured NetLicensing Gateway endpoint during the fulfilment process
- NetLicensing creates and assigns Licenses to the current Customer

### References:

- Typical e-commerce integration flow: https://github.com/Labs64/NetLicensing-Gateway#license-acquisition-flow
- Sample implementation (MyCommerce): https://github.com/Labs64/NetLicensing-Gateway/blob/master/src/main/java/com/labs64/netlicensing/gateway/controller/restful/MyCommerceController.java
- NetLicening RESTful API: https://netlicensing.io/wiki/restful-api
- NetLicensing test account: `demo:demo` or your own
- **E-commerce integration documentation:**
  - _ECOMM_DOC_URL_

---

### Support

Feel free to contact us for support via:
- Email: netlicensing@labs64.com
- Telegram chat: https://t.me/netlicensing
