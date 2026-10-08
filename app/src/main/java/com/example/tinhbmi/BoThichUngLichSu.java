package com.example.tinhbmi;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Đưa danh sách lịch sử đo lên RecyclerView; mỗi mục có nút Chia sẻ. */
public class BoThichUngLichSu extends RecyclerView.Adapter<BoThichUngLichSu.KhungMucLichSu> {

    private final Context nguCanh;
    private final List<BanGhiLichSu> danhSachLichSu;

    public BoThichUngLichSu(Context nguCanh, List<BanGhiLichSu> danhSachLichSu) {
        this.nguCanh = nguCanh;
        this.danhSachLichSu = danhSachLichSu;
    }

    @NonNull
    @Override
    public KhungMucLichSu onCreateViewHolder(@NonNull ViewGroup nhomCha, int loaiMuc) {
        View mucView = LayoutInflater.from(nguCanh).inflate(R.layout.muc_lich_su, nhomCha, false);
        return new KhungMucLichSu(mucView);
    }

    @Override
    public void onBindViewHolder(@NonNull KhungMucLichSu khung, int viTri) {
        BanGhiLichSu banGhi = danhSachLichSu.get(viTri);
        int mauSac = TienIch.layMauSac(nguCanh, banGhi.chiSoBmi);

        khung.nhanThoiGian.setText(TienIch.dinhDangThoiGian(banGhi.thoiGian));

        khung.nhanChiSoBmi.setText(String.format(TienIch.NGON_NGU_VIET, "%.1f", banGhi.chiSoBmi));
        khung.nhanChiSoBmi.setTextColor(mauSac);

        khung.nhanPhanLoai.setText(banGhi.phanLoai);
        GradientDrawable nenNhan = (GradientDrawable) khung.nhanPhanLoai.getBackground().mutate();
        nenNhan.setColor(mauSac);
        khung.nhanPhanLoai.setTextColor(TienIch.layMauChuTrenNhan(nguCanh, banGhi.chiSoBmi));

        khung.nhanChiTiet.setText(banGhi.chieuCao + " cm • " + banGhi.canNang + " kg");

        khung.nutChiaSe.setOnClickListener(v -> chiaSeBanGhi(banGhi));
    }

    @Override
    public int getItemCount() {
        return danhSachLichSu.size();
    }

    public void xoaTatCa() {
        danhSachLichSu.clear();
        notifyDataSetChanged();
    }

    /** Mở hộp chia sẻ gốc của Android để người dùng chọn Zalo, Messenger, SMS, Gmail... */
    private void chiaSeBanGhi(BanGhiLichSu banGhi) {
        Intent yDinhChiaSe = new Intent(Intent.ACTION_SEND);
        yDinhChiaSe.setType("text/plain");
        yDinhChiaSe.putExtra(Intent.EXTRA_SUBJECT, "Kết quả BMI của tôi");
        yDinhChiaSe.putExtra(Intent.EXTRA_TEXT, banGhi.taoNoiDungChiaSe());

        try {
            nguCanh.startActivity(Intent.createChooser(yDinhChiaSe, "Chia sẻ kết quả BMI qua..."));
        } catch (ActivityNotFoundException loi) {
            Toast.makeText(nguCanh, "Không tìm thấy ứng dụng để chia sẻ", Toast.LENGTH_SHORT).show();
        }
    }

    /** Giữ các view của một mục trong danh sách. */
    static class KhungMucLichSu extends RecyclerView.ViewHolder {
        final TextView nhanThoiGian;
        final TextView nhanChiSoBmi;
        final TextView nhanPhanLoai;
        final TextView nhanChiTiet;
        final TextView nutChiaSe;

        KhungMucLichSu(@NonNull View mucView) {
            super(mucView);
            nhanThoiGian = mucView.findViewById(R.id.nhan_thoi_gian_muc);
            nhanChiSoBmi = mucView.findViewById(R.id.nhan_chi_so_bmi_muc);
            nhanPhanLoai = mucView.findViewById(R.id.nhan_phan_loai_muc);
            nhanChiTiet = mucView.findViewById(R.id.nhan_chi_tiet_muc);
            nutChiaSe = mucView.findViewById(R.id.nut_chia_se);
        }
    }
}