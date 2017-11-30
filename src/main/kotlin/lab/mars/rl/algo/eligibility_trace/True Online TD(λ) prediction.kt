package lab.mars.rl.algo.eligibility_trace

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.State
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*

fun <E> FunctionApprox.`True Online TD(λ) prediction`(vFunc: LinearFunc<E>, trans: (State) -> E, λ: Double) {
    val X = vFunc.x
    val w = vFunc.w
    val d = X.numOfComponents
    var z = Matrix.column(d)
    var V_old = 0.0
    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var step = 0
        var s = started()
        var x = X(trans(s))
        while (s.isNotTerminal()) {
            step++
            val a = π(s)
            val (s_next, reward) = a.sample()
            val _x = X(trans(s_next))
            val V = (w.T * x).asScalar()
            val _V = (w.T * _x).asScalar()
            val δ = reward + γ * _V - V
            z = γ * λ * z + (1.0 - α * γ * λ * z.T * x) * x
            w += α * (δ + V - V_old) * z - α * (V - V_old) * x
            V_old = _V
            x = _x
            s = s_next
        }
        episodeListener(episode, step)
    }
}