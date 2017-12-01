package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.*

fun <E> FunctionApprox.`Actor-Critic with Eligibility Traces (episodic)`(
    π: ApproximateFunction<E>, trans: (State, Action<State>) -> E, α_θ: Double, λ_θ: Double,
    v: ApproximateFunction<E>, transV: (State) -> E, α_w: Double, λ_w: Double) {
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step = 0
        var s = started()
        var z_θ = Matrix.column(π.w.size)
        var z_w = Matrix.column(v.w.size)
        var γ_t = 1.0
        while (s.isNotTerminal()) {
            step++
            val a = rand(s.actions) { π(trans(s, it)) }
            val (s_next, reward) = a.sample()
            val δ = reward + γ * if (s_next.isTerminal()) 0.0 else v(transV(s_next)) - v(transV(s))
            z_w = γ * λ_w * z_w + γ_t * v.`▽`(transV(s))
            val `▽` = if (π is LinearFunc)
                π.x(trans(s, a)) - Σ(s.actions) { π(trans(s, it)) * π.x(trans(s, it)) }
            else
                π.`▽`(trans(s, a)) / π(trans(s, a))
            z_θ = γ * λ_θ * z_θ + γ_t * `▽`
            v.w += α_w * γ_t * δ * z_w
            π.w += α_θ * γ_t * δ * z_θ
            γ_t *= γ
            s = s_next
        }
    }
}