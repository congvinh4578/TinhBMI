package com.example.tinhbmi;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LichSuActivity extends AppCompatActivity {

    private View boCucGoc;
    private View khuVucTieuDe;
    private ImageButton nutQuayLai;
    private TextView nutXoaLichSu;
    private TextView nhanTrongLichSu;
    private RecyclerView khungDanhSachLichSu;

    private List<BanGhiLichSu> danhSachLichSu;
    private BoThichUngLichSu boThichUngLichSu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        KhoDuLieu.ungDungCheDoGiaoDien(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);

        anhXaView();
        TienIch.thietLapToanManHinh(this, boCucGoc, khuVucTieuDe, null);

        danhSachLichSu = KhoDuLieu.layLichSu(this);
        boThichUngLichSu = new BoThichUngLichSu(this, danhSachLichSu);
        khungDanhSachLichSu.setLayoutManager(new LinearLayoutManager(this));
        khungDanhSachLichSu.setAdapter(boThichUngLichSu);

        nutQuayLai.setOnClickListener(v -> finish());
        nutXoaLichSu.setOnClickListener(v -> hienHopThoaiXoaLichSu());

        capNhatTrangThaiTrong();
    }

    private void anhXaView() {
        boCucGoc = findViewById(R.id.bo_cuc_goc_lich_su);
        khuVucTieuDe = findViewById(R.id.khu_vuc_tieu_de_lich_su);
        nutQuayLai = findViewById(R.id.nut_quay_lai);
        nutXoaLichSu = findViewById(R.id.nut_xoa_lich_su);
        nhanTrongLichSu = findViewById(R.id.nhan_trong_lich_su);
        khungDanhSachLichSu = findViewById(R.id.khung_danh_sach_lich_su);
    }

    private void hienHopThoaiXoaLichSu() {
        if (danhSachLichSu.isEmpty()) {
            Toast.makeText(this, "Lịch sử đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa lịch sử")
                .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử đo không?")
                .setPositiveButton("Xóa", (hopThoai, nutBam) -> {
                    KhoDuLieu.xoaLichSu(this);
                    boThichUngLichSu.xoaTatCa();
                    capNhatTrangThaiTrong();
                    Toast.makeText(this, "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void capNhatTrangThaiTrong() {
        boolean dangTrong = danhSachLichSu.isEmpty();
        nhanTrongLichSu.setVisibility(dangTrong ? View.VISIBLE : View.GONE);
        khungDanhSachLichSu.setVisibility(dangTrong ? View.GONE : View.VISIBLE);
    }
}