package de.infonline.lib.iomb.util

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleSource
import io.reactivex.rxjava3.functions.Function4
import io.reactivex.rxjava3.functions.Function5
import io.reactivex.rxjava3.kotlin.Singles

internal fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any>
        Singles.zip(
        s1: SingleSource<T1>,
        s2: SingleSource<T2>,
        s3: SingleSource<T3>,
        s4: SingleSource<T4>
): Single<NTuple4<T1, T2, T3, T4>> = Single.zip(
        s1, s2, s3, s4,
        Function4 { t1, t2, t3, t4 -> NTuple4(t1, t2, t3, t4) }
)

internal fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any>
        Singles.zip(
        s1: SingleSource<T1>,
        s2: SingleSource<T2>,
        s3: SingleSource<T3>,
        s4: SingleSource<T4>,
        s5: SingleSource<T5>
): Single<NTuple5<T1, T2, T3, T4, T5>> = Single.zip(
        s1, s2, s3, s4, s5,
        Function5 { t1, t2, t3, t4, t5 -> NTuple5(t1, t2, t3, t4, t5) }
)