package edu.gatech.chai.fhir.smart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Component;

import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppEntry;
import edu.gatech.chai.fhir.smart.model.SmartOnFhirSessionEntry;

@Component
public class SmartOnFhirSessionImpl extends BaseSmartOnFhir implements SmartOnFhirSession {

	@Override
	public int save(SmartOnFhirSessionEntry sessionEntry) {
		String sql = "INSERT INTO SmartOnFhirSession (session_id, state, app_id, authorization_code, access_token, auth_code_expiration_dt, access_token_expiration_dt, refresh_token) values (?,?,?,?,?,?,?,?)";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionEntry.getSessionId());
			pstmt.setString(2, sessionEntry.getState());
			pstmt.setString(3, sessionEntry.getAppId());
			pstmt.setString(4, sessionEntry.getAuthorizationCode());
			pstmt.setString(5, sessionEntry.getAccessToken());
			pstmt.setDate(6, sessionEntry.getAuthCodeExpirationDT());
			pstmt.setDate(7, sessionEntry.getAccessTokenExpirationDT());
			pstmt.setString(8, sessionEntry.getRefreshToken());

			pstmt.executeUpdate();

			logger.info("Session Entry Created:\n" + printSessionInfo(sessionEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public void update(SmartOnFhirSessionEntry sessionEntry) {
		String sql = "UPDATE SmartOnFhirSession SET state=?, app_id=?, authorization_code=?, access_token=?, auth_code_expiration_dt=?, access_token_expiration_dt=?, refresh_token=? where session_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionEntry.getState());
			pstmt.setString(2, sessionEntry.getAppId());
			pstmt.setString(3, sessionEntry.getAuthorizationCode());
			pstmt.setString(4, sessionEntry.getAccessToken());
			pstmt.setDate(5, sessionEntry.getAuthCodeExpirationDT());
			pstmt.setDate(6, sessionEntry.getAccessTokenExpirationDT());
			pstmt.setString(7, sessionEntry.getRefreshToken());
			pstmt.setString(8, sessionEntry.getSessionId());
			pstmt.executeUpdate();
			logger.info("Session Entry Updated\n" + printSessionInfo(sessionEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void delete(String sessionId) {
		String sql = "DELETE FROM SmartOnFhirSession where session_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			pstmt.executeUpdate();
			logger.info("Session Entry (" + sessionId + ") deleted");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public void deleteByAppId(String appId) {
		String sql = "DELETE FROM SmartOnFhirSession where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			pstmt.executeUpdate();
			logger.info("Session Entry with appId (" + appId + ") deleted");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public void purgeOldSession() {
		// current time in epoch in milliseconds
		Calendar calendar = Calendar.getInstance();
		java.sql.Date now = new java.sql.Date(calendar.getTimeInMillis());

		// time in 5 days ago
		calendar.add(Calendar.DAY_OF_MONTH, -5);
		java.sql.Date skewed = new java.sql.Date(calendar.getTimeInMillis());

		String sql = "DELETE FROM SmartOnFhirSession WHERE (access_token is NULL) OR (access_token_expiration_dt<? "
				+ "AND refresh_token is NULL) OR (access_token_expiration_dt < ? AND auth_code_expiration_dt < ?)";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setDate(1, now);
			pstmt.setDate(2, skewed);
			pstmt.setDate(3, skewed);
			int count = pstmt.executeUpdate();

			logger.info(count + " sessions with old timestamps deleted");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

	}

	private SmartOnFhirSessionEntry createSessionEntry(ResultSet rs) throws SQLException {
		SmartOnFhirSessionEntry appEntry = new SmartOnFhirSessionEntry();
		appEntry.setSessionId(rs.getString("session_id"));
		appEntry.setAppId(rs.getString("app_id"));
		appEntry.setState(rs.getString("state"));
		appEntry.setAuthorizationCode(rs.getString("authorization_code"));
		appEntry.setAccessToken(rs.getString("access_token"));
		appEntry.setAuthCodeExpirationDT(rs.getDate("auth_code_expiration_dt"));
		appEntry.setAccessTokenExpirationDT(rs.getDate("access_token_expiration_dt"));
		appEntry.setRefreshToken(rs.getString("refresh_token"));

		return appEntry;
	}

	@Override
	public List<SmartOnFhirSessionEntry> get() {
		List<SmartOnFhirSessionEntry> appSessionList = new ArrayList<SmartOnFhirSessionEntry>();

		String sql = "SELECT * FROM SmartOnFhirSession";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				appSessionList.add(createSessionEntry(rs));
			}
			logger.info(appSessionList.size() + " app entries obtained");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appSessionList;
	}

	@Override
	public SmartOnFhirSessionEntry getSmartOnFhirSession(String sessionId) {
		SmartOnFhirSessionEntry appSession = null;

		String sql = "SELECT * FROM SmartOnFhirSession where session_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				appSession = createSessionEntry(rs);
				logger.info("App Session Obtained\n" + printSessionInfo(appSession));
			} else {
				logger.info("No App Session Exist with session-id = " + sessionId);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appSession;
	}

	public SmartOnFhirSessionEntry getSmartOnFhirSession(String appId, String authCode) {
		SmartOnFhirSessionEntry appSession = null;

		String sql = "SELECT * FROM SmartOnFhirSession where app_id=? and authorization_code=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			pstmt.setString(2, authCode);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				appSession = createSessionEntry(rs);
				logger.info("App Session Obtained\n" + printSessionInfo(appSession));
			} else {
				logger.info("No App Session Exist with session-id = " + appId);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appSession;
	}

	public List<SmartOnFhirSessionEntry> getSmartOnFhirSessionsByAppId(String appId) {
		List<SmartOnFhirSessionEntry> appSessions = new ArrayList<SmartOnFhirSessionEntry>();

		String sql = "SELECT * FROM SmartOnFhirSession where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				appSessions.add(createSessionEntry(rs));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return appSessions;
	}

	public SmartOnFhirSessionEntry getSmartOnFhirAppByToken(String token) {
		SmartOnFhirSessionEntry sessionEntry = null;

		String sql = "SELECT * FROM SmartOnFhirSession where access_token=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, token);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				sessionEntry = createSessionEntry(rs);
				logger.info("Session" + printSessionInfo(sessionEntry));
			} else {
				logger.info("No Session Entry Exist with access_token = " + token);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return sessionEntry;
	}

	public SmartOnFhirSessionEntry getSmartOnFhirAppByRefreshToken(String token) {
		SmartOnFhirSessionEntry sessionEntry = null;

		String sql = "SELECT * FROM SmartOnFhirSession where refresh_token=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, token);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				sessionEntry = createSessionEntry(rs);
				logger.info("Session" + printSessionInfo(sessionEntry));
			} else {
				logger.info("No Session Entry Exist with refresh token = " + token);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return sessionEntry;
	}

	public void putAccessCode(String appId, String authCode, String accessToken) {
		String sql = "UPDATE SmartOnFhirSession SET access_token=?, access_token_expiration_dt=? where app_id=? and authorization_code=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 5);
			java.sql.Date expiresIn = new java.sql.Date(calendar.getTimeInMillis());
			pstmt.setString(1, accessToken);
			pstmt.setDate(2, expiresIn);
			pstmt.setString(3, appId);
			pstmt.setString(4, authCode);
			pstmt.executeUpdate();

			logger.info("Access Token is updated\nAccess Token:" + accessToken + "\nexpires in:" + expiresIn);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public void putRefereshCode(String appId, String authCode, String refreshToken) {
		String sql = "UPDATE SmartOnFhirSession SET refresh_token=? where app_id=? and authorization_code=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, refreshToken);
			pstmt.setString(2, appId);
			pstmt.setString(3, authCode);
			pstmt.executeUpdate();

			logger.info("Refresh Token is updated\nRefresh Token:" + refreshToken);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public void updateAccessTokenTimeout(String sessionId, java.sql.Date timeoutDate) {
		String sql = "UPDATE SmartOnFhirSession SET access_token_expiration_dt=? where session_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setDate(1, timeoutDate);
			pstmt.setString(2, sessionId);
			pstmt.executeUpdate();

			logger.info("Access Token Timeout is updated\nSession Id:" + sessionId + " to " + timeoutDate);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public boolean exists(String sessionId) {
		String sql = "SELECT * FROM SmartOnFhirSession where session_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				return true;
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return false;
	}

	private String printSessionInfo(SmartOnFhirSessionEntry sessionEntry) {
		String appInfo = "session-id: " + sessionEntry.getAppId() + "\n" + "state: " + sessionEntry.getState() + "\n"
				+ "app-id: " + sessionEntry.getAppId() + "\n" + "authorization-code: "
				+ sessionEntry.getAuthorizationCode() + "\n" + "access-token: " + sessionEntry.getAccessToken() + "\n"
				+ "authorization-code-expiration_datatime: " + sessionEntry.getAuthCodeExpirationDT() + "\n"
				+ "access-token-expiration_datatime: " + sessionEntry.getAccessTokenExpirationDT() + "\n"
				+ "refresh-token: " + sessionEntry.getRefreshToken() + "\n";

		return appInfo;
	}

}
