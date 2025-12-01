/**
 * Email Service for Loyalty Tier Notifications
 * Sends email notifications when user tier changes
 */

const nodemailer = require("nodemailer");
const { getTierInfo, formatCurrencyExact } = require("../config/loyaltyTiers");

// Create email transporter (configure based on your email service)
const transporter = nodemailer.createTransport({
  service: process.env.EMAIL_SERVICE || "gmail",
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
});

/**
 * Send loyalty tier upgrade notification email
 * @param {object} user - User object with email, firstName, lastName
 * @param {string} newTier - New tier ID
 * @param {string} oldTier - Old tier ID
 * @returns {Promise<object>} Email send result
 */
const sendTierUpgradeEmail = async (user, newTier, oldTier) => {
  try {
    const newTierInfo = getTierInfo(newTier);
    const oldTierInfo = getTierInfo(oldTier);

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <style>
            body { font-family: Arial, sans-serif; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background-color: #4CAF50; color: white; padding: 20px; border-radius: 5px; }
            .content { padding: 20px; background-color: #f9f9f9; }
            .tier-badge { 
              display: inline-block; 
              padding: 10px 20px; 
              border-radius: 5px; 
              margin: 10px 0;
              font-weight: bold;
            }
            .bronze { background-color: #CD7F32; color: white; }
            .silver { background-color: #C0C0C0; color: black; }
            .gold { background-color: #FFD700; color: black; }
            .footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>🎉 Chúc mừng! Bạn đã nâng cấp cấp độ thân thiết</h1>
            </div>
            <div class="content">
              <p>Xin chào ${user.firstName} ${user.lastName},</p>
              
              <p>Chúng tôi rất vui thông báo rằng bạn đã nâng cấp cấp độ thân thiết từ:</p>
              
              <p>
                <span class="tier-badge ${oldTier}">${oldTierInfo.name}</span>
                <span style="margin: 0 10px;">→</span>
                <span class="tier-badge ${newTier}">${newTierInfo.name}</span>
              </p>
              
              <h3>Lợi ích của cấp độ ${newTierInfo.name}:</h3>
              <ul>
                <li>${newTierInfo.benefits}</li>
                <li>Chiết khấu tự động: ${
                  newTierInfo.discount
                }% trên các sản phẩm hợp lệ</li>
              </ul>
              
              <h3>Thông tin cấp độ của bạn:</h3>
              <ul>
                <li><strong>Cấp độ hiện tại:</strong> ${newTierInfo.name}</li>
                <li><strong>Tổng chi tiêu:</strong> ${formatCurrencyExact(
                  user.totalSpent
                )}</li>
              </ul>
              
              <p>Cảm ơn bạn đã là khách hàng trung thành của chúng tôi!</p>
              
              <p>Trân trọng,<br/>Đội ngũ Bach Hòa</p>
            </div>
            <div class="footer">
              <p>Email này được gửi tự động. Vui lòng không trả lời email này.</p>
            </div>
          </div>
        </body>
      </html>
    `;

    const mailOptions = {
      from: process.env.EMAIL_USER,
      to: user.email,
      subject: `🎉 Chúc mừng nâng cấp thành ${newTierInfo.name}!`,
      html: htmlContent,
    };

    const result = await transporter.sendMail(mailOptions);
    console.log(`✅ Tier upgrade email sent to ${user.email}`);
    return { success: true, result };
  } catch (error) {
    console.error("Error sending tier upgrade email:", error);
    return { success: false, error: error.message };
  }
};

/**
 * Send tier downgrade notification email
 * @param {object} user - User object with email, firstName, lastName
 * @param {string} currentTier - Current tier ID
 * @param {string} previousTier - Previous tier ID
 * @returns {Promise<object>} Email send result
 */
const sendTierDowngradeEmail = async (user, currentTier, previousTier) => {
  try {
    const currentTierInfo = getTierInfo(currentTier);
    const previousTierInfo = getTierInfo(previousTier);

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <style>
            body { font-family: Arial, sans-serif; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background-color: #FF9800; color: white; padding: 20px; border-radius: 5px; }
            .content { padding: 20px; background-color: #f9f9f9; }
            .tier-badge { 
              display: inline-block; 
              padding: 10px 20px; 
              border-radius: 5px; 
              margin: 10px 0;
              font-weight: bold;
            }
            .bronze { background-color: #CD7F32; color: white; }
            .silver { background-color: #C0C0C0; color: black; }
            .gold { background-color: #FFD700; color: black; }
            .footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h1>ℹ️ Thông báo thay đổi cấp độ thân thiết</h1>
            </div>
            <div class="content">
              <p>Xin chào ${user.firstName} ${user.lastName},</p>
              
              <p>Cấp độ thân thiết của bạn đã thay đổi từ:</p>
              
              <p>
                <span class="tier-badge ${previousTier}">${previousTierInfo.name}</span>
                <span style="margin: 0 10px;">→</span>
                <span class="tier-badge ${currentTier}">${currentTierInfo.name}</span>
              </p>
              
              <h3>Lợi ích của cấp độ ${currentTierInfo.name}:</h3>
              <ul>
                <li>${currentTierInfo.benefits}</li>
                <li>Chiết khấu tự động: ${currentTierInfo.discount}% trên các sản phẩm hợp lệ</li>
              </ul>
              
              <h3>Cách nâng cấp lại:</h3>
              <p>Để quay lại cấp độ ${previousTierInfo.name}, bạn cần tiếp tục mua sắm. Mỗi đơn hàng sẽ cộng vào tổng chi tiêu của bạn.</p>
              
              <p>Cảm ơn bạn đã tiếp tục ủng hộ chúng tôi!</p>
              
              <p>Trân trọng,<br/>Đội ngũ Bach Hòa</p>
            </div>
            <div class="footer">
              <p>Email này được gửi tự động. Vui lòng không trả lời email này.</p>
            </div>
          </div>
        </body>
      </html>
    `;

    const mailOptions = {
      from: process.env.EMAIL_USER,
      to: user.email,
      subject: `ℹ️ Thông báo thay đổi cấp độ`,
      html: htmlContent,
    };

    const result = await transporter.sendMail(mailOptions);
    console.log(`✅ Tier change email sent to ${user.email}`);
    return { success: true, result };
  } catch (error) {
    console.error("Error sending tier change email:", error);
    return { success: false, error: error.message };
  }
};

module.exports = {
  sendTierUpgradeEmail,
  sendTierDowngradeEmail,
};
