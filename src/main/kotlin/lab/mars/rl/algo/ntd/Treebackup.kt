package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy`
import lab.mars.rl.algo.ntd.NStepTemporalDifference.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.Sigma
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import org.apache.commons.math3.util.FastMath.min

fun NStepTemporalDifference.treebackup(alpha: (State, Action) -> Double = { _, _ -> this.alpha }): OptimalSolution {
    val pi = mdp.equiprobablePolicy()
    val Q = mdp.QFunc { 0.0 }

    val _Q = newBuf<Double>(min(n, MAX_N))
    val _Pi = newBuf<Double>(min(n, MAX_N))
    val _delta = newBuf<Double>(min(n, MAX_N))
    val _S = newBuf<State>(min(n, MAX_N))
    val _A = newBuf<Action>(min(n, MAX_N))

    for (episode in 1..episodes) {
        var n = n
        log.debug { "$episode/$episodes" }
        var T = Int.MAX_VALUE
        var t = 0
        var s = started.rand()
        var a = s.actions.rand(pi(s))

        _Q.clear(); _Q.append(0.0)
        _Pi.clear();_Pi.append(pi[s, a])
        _delta.clear()
        _S.clear();_S.append(s)
        _A.clear(); _A.append(a)

        do {
            if (t >= n) {//最多存储n个
                _Q.removeFirst()
                _Pi.removeFirst()
                _delta.removeFirst()
                _S.removeFirst()
                _A.removeFirst()
            }
            if (t < T) {
                val (s_next, reward, _) = a.sample()
                _S.append(s_next)
                s = s_next
                if (s.isTerminal()) {
                    _delta.append(reward - _Q.last)
                    T = t + 1
                    val _t = t - n + 1
                    if (_t < 0) n = T //n is too large, normalize it
                } else {
                    _delta.append(reward + gamma * Sigma(s.actions) { pi[s, it] * Q[s, it] } - _Q.last)
                    a = s.actions.rand()
                    _A.append(a)
                    _Q.append(Q[s, a])
                    _Pi.append(pi[s, a])
                }
            }
            val _t = t - n + 1
            if (_t >= 0) {
                var e = 1.0
                var G = _Q[0]
                val end = min(n - 1, T - 1 - _t)
                for (k in 0..end) {
                    G += e * _delta[k]
                    if (k < end) e *= gamma * _Pi[k + 1]
                }
                Q[_S[0], _A[0]] += alpha(_S[0], _A[0]) * (G - Q[_S[0], _A[0]])
                `e-greedy`(states[_S[0]], Q, pi, epsilon)
            }
            t++
        } while (_t < T - 1)
        log.debug { "n=$n,T=$T" }
    }
    val V = mdp.VFunc { 0.0 }
    val result = Triple(pi, V, Q)
    V_from_Q_ND(states, result)
    return result
}