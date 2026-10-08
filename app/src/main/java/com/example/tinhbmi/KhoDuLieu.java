package com.example.tinhbmi;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Lớp lưu trữ dữ liệu bằng SharedPreferences: giao diện sáng/tối và lịch sử đo. */
public final class KhoDuLieu {

    private static final String TEN_BO_NHO = "bo_nho_may_tinh_bmi";
    private static final String KHOA_CHE_DO_GIAO_DIEN = "che_do_giao_dien";
    private static final String KHOA_LICH_SU = "lich_su_do_bmi";

    /** Số bản ghi tối đa được giữ lại (bản ghi cũ nhất sẽ bị bỏ khi vượt quá). */
    public static final int SO_BAN_GHI_TOI_DA = 100;

    private KhoDuLieu() {
    }

    private static SharedPreferences layBoNho(Context nguCanh) {
        return nguCanh.getSharedPreferences(TEN_BO_NHO, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------
    // Giao diện sáng / tối
    // ------------------------------------------------------------
    public static int layCheDoGiaoDien(Context nguCanh) {
        return layBoNho(nguCanh).getInt(KHOA_CHE_DO_GIAO_DIEN,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void luuCheDoGiaoDien(Context nguCanh, int cheDo) {
        layBoNho(nguCanh).edit().putInt(KHOA_CHE_DO_GIAO_DIEN, cheDo).apply();
    }

    /** Áp dụng chế độ đã lưu. Gọi trước super.onCreate() của mỗi màn hình. */
    public static void ungDungCheDoGiaoDien(Context nguCanh) {
        AppCompatDelegate.setDefaultNightMode(layCheDoGiaoDien(nguCanh));
    }

    // ------------------------------------------------------------
    // Lịch sử đo (lưu dưới dạng chuỗi JSON, bản ghi mới nhất ở đầu danh sách)
    // ------------------------------------------------------------
    public static List<BanGhiLichSu> layLichSu(Context nguCanh) {
        List<BanGhiLichSu> danhSach = new ArrayList<>();
        String chuoiJson = layBoNho(nguCanh).getString(KHOA_LICH_SU, "[]");
        try {
            JSONArray mangJson = new JSONArray(chuoiJson);
            for (int i = 0; i < mangJson.length(); i++) {
                JSONObject doiTuong = mangJson.getJSONObject(i);
                danhSach.add(new BanGhiLichSu(
                        doiTuong.getLong("thoi_gian"),
                        doiTuong.getInt("chieu_cao"),
                        doiTuong.getInt("can_nang"),
                        (float) doiTuong.getDouble("chi_so_bmi"),
                        doiTuong.getString("phan_loai")));
            }
        } catch (JSONException loi) {
            // Dữ liệu bị hỏng: coi như chưa có lịch sử
            danhSach.clear();
        }
        return danhSach;
    }

    public static void themLichSu(Context nguCanh, BanGhiLichSu banGhiMoi) {
        List<BanGhiLichSu> danhSach = layLichSu(nguCanh);
        danhSach.add(0, banGhiMoi);
        while (danhSach.size() > SO_BAN_GHI_TOI_DA) {
            danhSach.remove(danhSach.size() - 1);
        }

        JSONArray mangJson = new JSONArray();
        try {
            for (BanGhiLichSu banGhi : danhSach) {
                JSONObject doiTuong = new JSONObject();
                doiTuong.put("thoi_gian", banGhi.thoiGian);
                doiTuong.put("chieu_cao", banGhi.chieuCao);
                doiTuong.put("can_nang", banGhi.canNang);
                doiTuong.put("chi_so_bmi", (double) banGhi.chiSoBmi);
                doiTuong.put("phan_loai", banGhi.phanLoai);
                mangJson.put(doiTuong);
            }
        } catch (JSONException loi) {
            return;
        }
        layBoNho(nguCanh).edit().putString(KHOA_LICH_SU, mangJson.toString()).apply();
    }

    public static void xoaLichSu(Context nguCanh) {
        layBoNho(nguCanh).edit().remove(KHOA_LICH_SU).apply();
    }
}