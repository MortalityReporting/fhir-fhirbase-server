package edu.gatech.chai.fhir.smart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppEntry;

@Component
public class SmartOnFhirAppImpl extends BaseSmartOnFhir implements SmartOnFhirApp {

	@Override
	public int save(SmartOnFhirAppEntry appEntry) {
		String sql = "INSERT INTO SmartOnFhirApp (app_id, app_name, app_type, redirect_uri, launch_uri, scope) values (?,?,?,?,?,?)";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appEntry.getAppId());
			pstmt.setString(2, appEntry.getAppName());
			pstmt.setString(3, appEntry.getAppType());
			pstmt.setString(4, appEntry.getRedirectUri());
			pstmt.setString(5, appEntry.getLaunchUri());
			pstmt.setString(6, appEntry.getScope());

			pstmt.executeUpdate();

			logger.info("App Entry Created:\n" + printAppInfo(appEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public void update(SmartOnFhirAppEntry appEntry) {
		String sql = "UPDATE SmartOnFhirApp SET app_name=?, app_type=?, redirect_uri=?, launch_uri=?, scope=? where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appEntry.getAppName());
			pstmt.setString(2, appEntry.getAppType());
			pstmt.setString(3, appEntry.getRedirectUri());
			pstmt.setString(4, appEntry.getLaunchUri());
			pstmt.setString(5, appEntry.getScope());
			pstmt.setString(6, appEntry.getAppId());
			pstmt.executeUpdate();
			logger.info("App Entry Updated\n" + printAppInfo(appEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void delete(String appId) {
		String sql = "DELETE FROM SmartOnFhirApp where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			pstmt.executeUpdate();
			logger.info("App Entry (" + appId + ") deleted");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	private SmartOnFhirAppEntry createAppEntry(ResultSet rs) throws SQLException {
		SmartOnFhirAppEntry appEntry = new SmartOnFhirAppEntry();
		appEntry.setAppId(rs.getString("app_id"));
		appEntry.setAppName(rs.getString("app_name"));
		appEntry.setAppType(rs.getString("app_type"));
		appEntry.setRedirectUri(rs.getString("redirect_uri"));
		appEntry.setLaunchUri(rs.getString("launch_uri"));
		appEntry.setScope(rs.getString("scope"));
		
		return appEntry;
	}
	
	@Override
	public List<SmartOnFhirAppEntry> get() {
		List<SmartOnFhirAppEntry> appEntryList = new ArrayList<SmartOnFhirAppEntry>();

		String sql = "SELECT * FROM SmartOnFhirApp";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				SmartOnFhirAppEntry appEntry = createAppEntry(rs);
				appEntryList.add(appEntry);
			}
			logger.info(appEntryList.size() + " app entries obtained");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appEntryList;
	}

	@Override
	public SmartOnFhirAppEntry getSmartOnFhirApp(String appId) {
		SmartOnFhirAppEntry appEntry = null;

		String sql = "SELECT * FROM SmartOnFhirApp where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				appEntry = createAppEntry(rs);
				logger.info("App Entry Obtained\n" + printAppInfo(appEntry));
			} else {
				logger.info("No App Entry Exist with app-id = " + appId);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appEntry;
	}

	public SmartOnFhirAppEntry getSmartOnFhirApp(String appId, String redirectUri) {
		SmartOnFhirAppEntry appEntry = null;

		String sql = "SELECT * FROM SmartOnFhirApp where app_id=? and redirect_uri=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			pstmt.setString(2, redirectUri);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				appEntry = createAppEntry(rs);
				logger.info("App Entry Obtained\n" + printAppInfo(appEntry));
			} else {
				logger.info("No App Entry Exist with app-id = " + appId);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appEntry;
	}

//	public SmartOnFhirAppEntry getSmartOnFhirAppByToken(String token) {
//		SmartOnFhirAppEntry appEntry = null;
//
//		String sql = "SELECT * FROM SmartOnFhirApp where access_token=?";
//
//		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//			pstmt.setString(1, token);
//			ResultSet rs = pstmt.executeQuery();
//
//			if (rs.next()) {
//				appEntry = createAppEntry(rs);
//				logger.info("App Entry Obtained\n" + printAppInfo(appEntry));
//			} else {
//				logger.info("No App Entry Exist with access_token = " + token);
//			}
//		} catch (SQLException e) {
//			logger.error(e.getMessage());
//		}
//
//		return appEntry;
//	}

//	public void putAcessCode(String appId, String accessToken) {
//		String sql = "UPDATE SmartOnFhirApp SET access_token=?, access_token_expire=? where app_id=?";
//
//		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.MINUTE, 5);
//			java.sql.Date expiresIn = new java.sql.Date(calendar.getTimeInMillis());
//			pstmt.setString(1, accessToken);
//			pstmt.setDate(2, expiresIn);
//			pstmt.setString(3, appId);
//			pstmt.executeUpdate();
//			
//			logger.info("AuthCode is updated\nAuth code:" + accessToken + "\nexpires in:" + expiresIn);
//		} catch (SQLException e) {
//			logger.error(e.getMessage());
//		}
//	}
	
//	public void putAuthorizationCode(String appId, String authCode) {
//		String sql = "UPDATE SmartOnFhirApp SET authorization_code=?, auth_code_expire=? where app_id=?";
//
//		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.MINUTE, 5);
//			java.sql.Date expiresIn = new java.sql.Date(calendar.getTimeInMillis());
//			pstmt.setString(1, authCode);
//			pstmt.setDate(2, expiresIn);
//			pstmt.setString(3, appId);
//			pstmt.executeUpdate();
//			
//			logger.info("AuthCode is updated\nAuth code:" + authCode + "\nexpires in:" + expiresIn);
//		} catch (SQLException e) {
//			logger.error(e.getMessage());
//		}
//	}
//	
	public boolean exists(String appId) {
		String sql = "SELECT * FROM SmartOnFhirApp where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return true;
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		
		return false;
	}
	
	private String printAppInfo(SmartOnFhirAppEntry appEntry) {
		String appInfo = "client-id: " + appEntry.getAppId() + "\n" 
				+ "app-name: " + appEntry.getAppName() + "\n"
				+ "app-type: " + appEntry.getAppType() + "\n"
				+ "redirect-uri: " + appEntry.getRedirectUri() + "\n" 
				+ "launch-uri: " + appEntry.getLaunchUri() + "\n"
				+ "scope: " + appEntry.getScope() + "\n";

		return appInfo;
	}
}
