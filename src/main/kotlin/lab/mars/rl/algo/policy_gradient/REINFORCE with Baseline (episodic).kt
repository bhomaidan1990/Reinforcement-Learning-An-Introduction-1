package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.matrix.Σ
import kotlin.math.exp

fun <E> MDP.`REINFORCE with Baseline (episodic)`(
    h: LinearFunc<E>, α_θ: Double,
    v: ApproximateFunction<E>, α_w: Double,
    episodes: Int,
    episodeListener: (Int, Int, State, Double) -> Unit = { _, _, _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = rand(s.actions) { exp(h(s, it)) }
    val S = newBuf<State>()
    val A = newBuf<Action<State>>()
    val R = newBuf<Double>()
    
    S.append(s)
    R.append(0.0)
    var accu = 0.0
    var T: Int
    while (true) {
      step++
      A.append(a)
      val (s_next, reward) = a.sample()
      accu += reward
      stepListener(episode, step, s, a)
      R.append(accu)
      S.append(s_next)
      s = s_next
      if (s_next.isTerminal) {
        T = step
        break
      }
      
      a = rand(s.actions) { exp(h(s, it)) }
    }
    var γ_t = 1.0
    for (t in 0 until T) {
      val G = accu - R[t]
      val δ = G - v(S[t])
      v.w += α_w * γ_t * δ * v.`∇`(S[t])
      val `∇` = h.x(S[t], A[t]) - Σ(S[t].actions) { b ->
        val tmp = exp(h(S[t], b))
        h.x(S[t], b) / S[t].actions.sumByDouble { exp(h(S[t], it) - tmp) }
      }
      h.w += α_θ * γ_t * δ * `∇`
      γ_t *= γ
    }
    episodeListener(episode, T, s, accu)
  }
}