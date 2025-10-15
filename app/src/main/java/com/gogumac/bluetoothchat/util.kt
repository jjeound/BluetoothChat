package com.gogumac.bluetoothchat

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Context에서 Activity를 찾아 반환하는 확장 함수.
 * - `ContextWrapper`를 통해 상위 `Context`를 탐색하며 `Activity`를 찾음.
 * - 만약 `Activity`를 찾을 수 없으면 `IllegalStateException`을 발생시킴.
 *
 * @receiver Context 현재 `Context`
 * @return Activity 찾은 `Activity`
 * @throws IllegalStateException `Activity`를 찾을 수 없는 경우 예외 발생
 */
fun Context.findActivity(): Activity {
    var context = this // 현재 Context에서 시작

    // Context가 ContextWrapper의 인스턴스일 경우 계속해서 상위 Context를 탐색
    while (context is ContextWrapper) {
        // 현재 Context가 Activity라면 반환
        if (context is Activity) return context

        // 다음 상위 Context로 이동
        context = context.baseContext
    }

    // Activity를 찾지 못하면 예외 발생
    throw IllegalStateException("No Activity found in the given Context hierarchy")
}
