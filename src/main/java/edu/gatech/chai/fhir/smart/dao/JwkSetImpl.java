package edu.gatech.chai.fhir.smart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import edu.gatech.chai.fhir.smart.model.JwkSetEntry;

@Component
public class JwkSetImpl extends BaseSmartOnFhir implements JwKSet {

	@Override
	public int save(JwkSetEntry jwkSetEntry) {
		String sql = "INSERT INTO JWKSets (app_id, public_key, kid, kty, jti, exp, jwk_raw) values (?,?,?,?,?,?,?)";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, jwkSetEntry.getAppId());
			pstmt.setString(2, jwkSetEntry.getPublicKey());
			pstmt.setString(3, jwkSetEntry.getKid());
			pstmt.setString(4, jwkSetEntry.getKty());
			pstmt.setString(5, jwkSetEntry.getJti());
			pstmt.setInt(6, jwkSetEntry.getExp());
			pstmt.setString(7, jwkSetEntry.getJwkRaw());

			pstmt.executeUpdate();

			logger.info("JWK Set Entry Created:\n" + JwkSetImpl.printAppInfo(jwkSetEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public void update(JwkSetEntry jwkSetEntry) {
		String sql = "UPDATE JWKSets SET public_key=?, kid=?, kty=?, jti=?, exp=?, jwk_raw=? where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, jwkSetEntry.getPublicKey());
			pstmt.setString(2, jwkSetEntry.getKid());
			pstmt.setString(3, jwkSetEntry.getKty());
			pstmt.setString(4, jwkSetEntry.getJti());
			pstmt.setInt(5, jwkSetEntry.getExp());
			pstmt.setString(6, jwkSetEntry.getJwkRaw());
			pstmt.setString(7, jwkSetEntry.getAppId());
			pstmt.executeUpdate();
			logger.info("JWK Set Entry Updated\n" + JwkSetImpl.printAppInfo(jwkSetEntry));
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void delete(String appId) {
		String sql = "DELETE FROM JWKSets where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			pstmt.executeUpdate();
			logger.info("JWK Set Entry (" + appId + ") deleted");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	private JwkSetEntry createAppEntry(ResultSet rs) throws SQLException {
		JwkSetEntry jwkSetEntry = new JwkSetEntry();
		jwkSetEntry.setAppId(rs.getString("app_id"));
		jwkSetEntry.setPublicKey(rs.getString("public_key"));
		jwkSetEntry.setKid(rs.getString("kid"));
		jwkSetEntry.setKty(rs.getString("kty"));
		jwkSetEntry.setJti(rs.getString("jti"));
		jwkSetEntry.setExp(rs.getInt("exp"));
		jwkSetEntry.setJwkRaw(rs.getString("jwk_raw"));

		return jwkSetEntry;
	}

	@Override
	public List<JwkSetEntry> get() {
		List<JwkSetEntry> jwkSetEntryList = new ArrayList<JwkSetEntry>();

		String sql = "SELECT * FROM JWKSets";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				JwkSetEntry jwkSetEntry = createAppEntry(rs);
				jwkSetEntryList.add(jwkSetEntry);
			}
			logger.info(jwkSetEntryList.size() + " JWK set entries obtained");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return jwkSetEntryList;
	}

	@Override
	public List<JwkSetEntry> getJwkSetByAppId(String appId) {
		List<JwkSetEntry> jwkSetEntryList = new ArrayList<JwkSetEntry>();

		String sql = "SELECT * FROM JWKSets where app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, appId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				JwkSetEntry jwkSetEntry = createAppEntry(rs);
				jwkSetEntryList.add(jwkSetEntry);
			}

			logger.info(jwkSetEntryList.size() + " JWK set entries obtained for client-id = " + appId);

		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return jwkSetEntryList;
	}

	@Override
	public List<JwkSetEntry> getJwkSetByKidAndIss(String kid, String iss) {
		List<JwkSetEntry> jwkSetEntryList = new ArrayList<JwkSetEntry>();

		String sql = "SELECT * FROM JWKSets where kid=? and app_id=?";

		try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, kid);
			pstmt.setString(2, iss);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				JwkSetEntry jwkSetEntry = createAppEntry(rs);
				jwkSetEntryList.add(jwkSetEntry);
			}
			logger.info(jwkSetEntryList.size() + " JWK set entries obtained for kid = " + kid + "and iss = "+ iss);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		return jwkSetEntryList;
	}

	public static String printAppInfo(JwkSetEntry jwkEntry) {
		String jwkInfo = "client-id: " + jwkEntry.getAppId() + "\n" + "public-key: " + jwkEntry.getPublicKey() + "\n"
				+ "kid: " + jwkEntry.getKid() + "\n" + "kty: " + jwkEntry.getKty() + "\n" + "jti:" + jwkEntry.getKty()
				+ "\n" + "exp:" + jwkEntry.getExp() + "\n" + "JWK raw: " + jwkEntry.getJwkRaw() + "\n";

		return jwkInfo;
	}

}
