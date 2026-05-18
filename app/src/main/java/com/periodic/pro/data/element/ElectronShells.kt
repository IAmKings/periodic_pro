package com.periodic.pro.data.element

/**
 * 电子壳层分布计算。
 *
 * 根据原子序数返回每层电子数（K=2, L=8, M=18, N=32, O=32, P=18, Q=8），
 * 简化计算：按轨道能量顺序填充，不处理 4s/3d 交错等细节。
 */
object ElectronShells {

    /** 每层最大电子数 */
    private val shellCapacity = listOf(2, 8, 18, 32, 32, 18, 8)

    /** 壳层标签 */
    val shellNames = listOf("K", "L", "M", "N", "O", "P", "Q")

    /**
     * 计算电子壳层分布。
     *
     * @param atomicNumber 原子序数
     * @return 每层电子数列表（只返回有电子的层）
     */
    fun getElectronShells(atomicNumber: Int): List<Int> {
        var remaining = atomicNumber
        val result = mutableListOf<Int>()
        for (cap in shellCapacity) {
            if (remaining <= 0) break
            val count = remaining.coerceAtMost(cap)
            result.add(count)
            remaining -= count
        }
        return result
    }
}
